package de.rki.coronawarnapp.vaccination.core.execution.task

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class VaccinationUpdateTask @Inject constructor(
    private val timeStamper: TimeStamper,
    private val vaccinationRepository: VaccinationRepository,
) : Task<DefaultProgress, VaccinationUpdateTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.d("Running with arguments=%s", arguments)

        doWork()
    } catch (error: Exception) {
        Timber.tag(TAG).e(error)
        error.reportProblem(TAG, "Vaccination update task failed.")
        throw error
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.close()
    }

    private suspend fun doWork(): Result {
        // TODO

        return Result
    }

    private fun checkCancel() {
        if (isCanceled) throw TaskCancellationException()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    object Result : Task.Result

    data class Config(
        override val executionTimeout: Duration = Duration.standardMinutes(9),
        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING
    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<VaccinationUpdateTask>,
        private val appConfigProvider: AppConfigProvider
    ) : TaskFactory<DefaultProgress, Result> {

        override suspend fun createConfig(): TaskFactory.Config = Config(
            executionTimeout = appConfigProvider.getAppConfig().overallDownloadTimeout
        )

        override val taskProvider: () -> Task<DefaultProgress, Result> = {
            taskByDagger.get()
        }
    }

    companion object {
        private const val TAG = "VaccinationUpdateTask"
    }
}
