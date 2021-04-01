package de.rki.coronawarnapp.presencetracing.warning.execution

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.risk.CheckInWarningMatcher
import de.rki.coronawarnapp.presencetracing.warning.download.TraceWarningPackageSyncTool
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class PresenceTracingWarningTask @Inject constructor(
    private val timeStamper: TimeStamper,
    private val syncTool: TraceWarningPackageSyncTool,
    private val checkInWarningMatcher: CheckInWarningMatcher,
) : Task<DefaultProgress, PresenceTracingWarningTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.d("Running with arguments=%s", arguments)
        val nowUTC = timeStamper.nowUTC

        Timber.tag(TAG).d("Running package sync.")
        syncTool.syncPackages()

        checkCancel()

        Timber.tag(TAG).d("Running check-in matcher.")
        checkInWarningMatcher.execute()

        Result(calculatedAt = nowUTC)
    } catch (error: Exception) {
        Timber.tag(TAG).e(error)
        error.report(ExceptionCategory.EXPOSURENOTIFICATION)
        throw error
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.close()
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
        override val executionTimeout: Duration = Duration.standardMinutes(8), // TODO unit-test that not > 9 min

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior = TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING

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
        private const val TAG = "TraceTimeWarningTask"
    }
}
