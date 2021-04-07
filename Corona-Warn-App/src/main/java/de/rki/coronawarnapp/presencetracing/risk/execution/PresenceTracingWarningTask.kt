package de.rki.coronawarnapp.presencetracing.risk.execution

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningMatcher
import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.presencetracing.warning.download.TraceWarningPackageSyncTool
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class PresenceTracingWarningTask @Inject constructor(
    private val timeStamper: TimeStamper,
    private val syncTool: TraceWarningPackageSyncTool,
    private val checkInWarningMatcher: CheckInWarningMatcher,
    private val presenceTracingRiskRepository: PresenceTracingRiskRepository,
    private val traceWarningRepository: TraceWarningRepository,
    private val checkInsRepository: CheckInRepository,
) : Task<DefaultProgress, PresenceTracingWarningTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.d("Running with arguments=%s", arguments)

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
        internalProgress.close()
    }

    private suspend fun doWork(): Result {
        val nowUTC = timeStamper.nowUTC

        Timber.tag(TAG).d("Running package sync.")
        syncTool.syncPackages()

        checkCancel()

        presenceTracingRiskRepository.deleteStaleData()

        val checkIns = checkInsRepository.allCheckIns.firstOrNull() ?: emptyList()
        Timber.tag(TAG).d("There are %d check-ins to match against.", checkIns.size)

        if (checkIns.isEmpty()) {
            Timber.tag(TAG).i("No check-ins available. We are done.")
            presenceTracingRiskRepository.reportCalculation(successful = true)

            return Result(calculatedAt = nowUTC)
        }

        val unprocessedPackages = traceWarningRepository.unprocessedWarningPackages.firstOrNull() ?: emptyList()
        Timber.tag(TAG).d("There are %d unprocessed warning packages.", unprocessedPackages.size)

        if (unprocessedPackages.isEmpty()) {
            Timber.tag(TAG).i("No new warning packages available.")

            presenceTracingRiskRepository.reportCalculation(successful = true)

            return Result(calculatedAt = nowUTC)
        }

        Timber.tag(TAG).d("Running check-in matcher.")
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
            overlaps = overlapsDistinct,
        )

        // markPackagesProcessed only after reportCalculation, if there is an exception, then we can process again.
        traceWarningRepository.markPackagesProcessed(
            matcherResult.processedPackages.map { it.warningPackage.packageId }
        )

        return Result(calculatedAt = nowUTC)
    }

    private fun checkCancel() {
        if (isCanceled) throw TaskCancellationException()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    data class Result(
        val calculatedAt: Instant
    ) : Task.Result

    data class Config(
        override val executionTimeout: Duration = Duration.standardMinutes(9),
        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING
    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<PresenceTracingWarningTask>,
        private val appConfigProvider: AppConfigProvider
    ) : TaskFactory<DefaultProgress, Task.Result> {

        override suspend fun createConfig(): TaskFactory.Config = Config(
            executionTimeout = appConfigProvider.getAppConfig().overallDownloadTimeout
        )

        override val taskProvider: () -> Task<DefaultProgress, Task.Result> = {
            taskByDagger.get()
        }
    }

    companion object {
        private const val TAG = "TracingWarningTask"
    }
}
