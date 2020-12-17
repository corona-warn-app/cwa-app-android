package de.rki.coronawarnapp.contactdiary.retention

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

/**
 * Periodic background contact diary clean worker
 */
class ContactDiaryRetentionWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskController: TaskController
) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("Background job started. No backoff criteria")
        try {
            taskController.submitBlocking(
                DefaultTaskRequest(
                    ContactDiaryCleanTask::class,
                    originTag = "ContactDiaryCleanWorker"
                )
            ).error?.also { error: Throwable ->
                Timber.tag(TAG).w(error, "$id: Error when cleaning contact diary.")
                return Result.failure()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).d(e)
            return Result.failure()
        }
        Timber.tag(TAG).d("Background job completed")
        return Result.success()
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<ContactDiaryRetentionWorker>

    companion object {
        private val TAG = ContactDiaryRetentionWorker::class.java.simpleName
    }
}
