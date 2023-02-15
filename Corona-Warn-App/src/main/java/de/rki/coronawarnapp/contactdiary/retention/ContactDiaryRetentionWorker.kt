package de.rki.coronawarnapp.contactdiary.retention

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import timber.log.Timber

/**
 * Periodic background contact diary clean worker
 */
@HiltWorker
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

    companion object {
        private val TAG = tag<ContactDiaryRetentionWorker>()
    }
}
