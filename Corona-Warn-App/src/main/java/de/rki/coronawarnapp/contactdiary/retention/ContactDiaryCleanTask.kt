package de.rki.coronawarnapp.contactdiary.retention

import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.task.common.Finished
import de.rki.coronawarnapp.task.common.Started
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject
import javax.inject.Provider

class ContactDiaryCleanTask @Inject constructor(
    private val retentionCalculation: ContactDiaryRetentionCalculation
) : Task<DefaultProgress, Task.Result> {

    private val internalProgress = MutableStateFlow<DefaultProgress>(Started)
    override val progress: Flow<DefaultProgress> = internalProgress

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments) = try {
        Timber.d("Running with arguments=%s", arguments)

        retentionCalculation.run {
            clearObsoleteContactDiaryLocationVisits()
            Timber.tag(TAG).d("Obsolete contact diary location visits cleaned up")

            clearObsoleteContactDiaryPersonEncounters()
            Timber.tag(TAG).d("Obsolete contact diary person encounters cleaned up")

            clearObsoleteRiskPerDate()
            Timber.tag(TAG).d("Obsolete Aggregated Risk Per Date Results cleaned up")

            clearObsoleteCoronaTests()
            Timber.tag(TAG).d("Obsolete Contact Diary Corona Tests cleaned up")

            clearObsoleteSubmissions()
            Timber.tag(TAG).d("Obsolete Contact Diary Submissions cleaned up")
        }

        object : Task.Result {}
    } catch (error: Exception) {
        Timber.tag(TAG).e(error)
        throw error
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.value = Finished
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    class Config : TaskFactory.Config {
        override val executionTimeout: Duration = Duration.ofMinutes(9)
        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING
        override val errorHandling: TaskFactory.Config.ErrorHandling = TaskFactory.Config.ErrorHandling.SILENT
    }

    class Factory @Inject constructor(
        private val taskByDagger: Provider<ContactDiaryCleanTask>
    ) : TaskFactory<DefaultProgress, Task.Result> {

        override suspend fun createConfig(): TaskFactory.Config = Config()

        override val taskProvider: () -> Task<DefaultProgress, Task.Result> = {
            taskByDagger.get()
        }
    }

    companion object {
        private val TAG = tag<ContactDiaryCleanTask>()
    }
}
