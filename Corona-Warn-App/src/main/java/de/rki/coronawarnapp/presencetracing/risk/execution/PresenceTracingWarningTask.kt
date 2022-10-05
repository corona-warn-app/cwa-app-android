package de.rki.coronawarnapp.presencetracing.risk.execution

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOut
import de.rki.coronawarnapp.presencetracing.risk.CheckInsFilter
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningMatcher
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingRiskMapper
import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.presencetracing.warning.download.TraceWarningPackageSyncTool
import de.rki.coronawarnapp.presencetracing.warning.download.server.TraceWarningApi
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject
import javax.inject.Provider

@Suppress("LongParameterList")
class PresenceTracingWarningTask @Inject constructor(
    private val syncTool: TraceWarningPackageSyncTool,
    private val checkInWarningMatcher: CheckInWarningMatcher,
    private val presenceTracingRiskRepository: PresenceTracingRiskRepository,
    private val traceWarningRepository: TraceWarningRepository,
    private val checkInsRepository: CheckInRepository,
    private val presenceTracingRiskMapper: PresenceTracingRiskMapper,
    private val autoCheckOut: AutoCheckOut,
    private val appConfigProvider: AppConfigProvider,
    private val checkInsFilter: CheckInsFilter,
) : Task<PresenceTracingWarningTaskProgress, PresenceTracingWarningTask.Result> {

    private val internalProgress =
        MutableStateFlow<PresenceTracingWarningTaskProgress>(PresenceTracingWarningTaskProgress.Started)
    override val progress: Flow<PresenceTracingWarningTaskProgress> = internalProgress

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.d("Running with arguments=%s", arguments)

        autoCheckOut.apply {
            Timber.tag(TAG).d("Processing overdue check-outs before risk calculation.")
            processOverDueCheckouts()
            refreshAlarm()
        }

        try {
            doWork()
        } catch (e: Exception) {
            // We need to reported a failed calculation to update the risk card state
            presenceTracingRiskRepository.reportCalculation(successful = false)
            throw e
        }
    } catch (error: Exception) {
        Timber.tag(TAG).e(error)
        error.report(ExceptionCategory.EXPOSURENOTIFICATION)
        throw error
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.value = PresenceTracingWarningTaskProgress.Finished
    }

    private suspend fun doWork(): Result {
        checkCancel()

        // Resetting config to make sure latest changes are considered.
        presenceTracingRiskMapper.clearConfig()

        Timber.tag(TAG).d("Syncing packages.")
        internalProgress.value = PresenceTracingWarningTaskProgress.Downloading()

        val appConfig = appConfigProvider.getAppConfig()

        val unencryptedEnabled = appConfig.isUnencryptedCheckInsEnabled
        Timber.d("unencryptedEnabled=%s", unencryptedEnabled)

        val mode = if (unencryptedEnabled) TraceWarningApi.Mode.UNENCRYPTED else TraceWarningApi.Mode.ENCRYPTED
        Timber.d("TraceWarningApiMode=%s", mode)

        val syncResult = syncTool.syncPackages(mode)

        if (syncResult.successful) {
            Timber.tag(TAG).d("TraceWarningPackage sync successful: %s", syncResult)
        } else {
            Timber.tag(TAG).w("WarningPackage sync failed: %s", syncResult)
            presenceTracingRiskRepository.reportCalculation(successful = false)
            return Result()
        }

        presenceTracingRiskRepository.deleteStaleData()

        val checkIns = checkInsFilter.filterCheckIns(
            checkInsRepository.checkInsWithinRetention.firstOrNull() ?: emptyList()
        )

        Timber.tag(TAG).d("There are %d check-ins to match against.", checkIns.size)

        if (checkIns.isEmpty()) {
            Timber.tag(TAG).i("No check-ins available. Deleting all matches.")
            presenceTracingRiskRepository.deleteAllMatches()
            presenceTracingRiskRepository.reportCalculation(successful = true)
            return Result()
        }

        val unprocessedPackages = traceWarningRepository.unprocessedWarningPackages.firstOrNull() ?: emptyList()
        Timber.tag(TAG).d("There are %d unprocessed warning packages.", unprocessedPackages.size)

        if (unprocessedPackages.isEmpty()) {
            Timber.tag(TAG).i("No new warning packages available.")
            presenceTracingRiskRepository.reportCalculation(successful = true)
            return Result()
        }

        Timber.tag(TAG).d("Running check-in matcher.")
        internalProgress.value = PresenceTracingWarningTaskProgress.Calculating()

        val matcherResult = checkInWarningMatcher.process(
            checkIns = checkIns,
            warningPackages = unprocessedPackages,
        )
        Timber.tag(TAG).i("Check-in matcher result: %s", matcherResult)

        val overlaps = matcherResult.processedPackages.flatMap { it.overlaps }
        val overlapsDistinct = overlaps.distinct()
        if (overlaps.size != overlapsDistinct.size) {
            IllegalArgumentException("Matched overlaps are not distinct").also {
                it.reportProblem(TAG, "CheckInWarningMatcher results are not distinct.")
            }
        }

        // Partial processing: if calculation was not successful, but some packages were processed, we still save them
        presenceTracingRiskRepository.reportCalculation(
            successful = matcherResult.successful,
            newOverlaps = overlapsDistinct,
        )

        // markPackagesProcessed only after reportCalculation, if there is an exception, then we can process again.
        traceWarningRepository.markPackagesProcessed(
            matcherResult.processedPackages.map { it.warningPackage.packageId }
        )

        return Result()
    }

    private fun checkCancel() {
        if (isCanceled) throw TaskCancellationException()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    class Result : Task.Result

    data class Config(
        override val executionTimeout: Duration = Duration.ofMinutes(9),
        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING
    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<PresenceTracingWarningTask>,
        private val appConfigProvider: AppConfigProvider
    ) : TaskFactory<PresenceTracingWarningTaskProgress, Task.Result> {

        override suspend fun createConfig(): TaskFactory.Config = Config(
            executionTimeout = appConfigProvider.getAppConfig().overallDownloadTimeout
        )

        override val taskProvider: () -> Task<PresenceTracingWarningTaskProgress, Task.Result> = {
            taskByDagger.get()
        }
    }

    companion object {
        private const val TAG = "TracingWarningTask"
    }
}
