package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

/**
 * One time diagnosis key retrieval work
 * Executes the retrieve diagnosis key transaction
 *
 * @see BackgroundWorkScheduler
 */
class DiagnosisKeyRetrievalOneTimeWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskController: TaskController
) : CoroutineWorker(context, workerParams) {

    /**
     * Work execution
     *
     * @return Result
     */
    override suspend fun doWork(): Result {
        Timber.d("$id: doWork() started. Run attempt: $runAttemptCount")

        BackgroundWorkHelper.sendDebugNotification(
            "KeyOneTime Executing: Start", "KeyOneTime started. Run attempt: $runAttemptCount "
        )

        var result = Result.success()
        taskController.submitBlocking(
            DefaultTaskRequest(
                DownloadDiagnosisKeysTask::class,
                DownloadDiagnosisKeysTask.Arguments(),
                originTag = "DiagnosisKeyRetrievalOneTimeWorker"
            )
        ).error?.also { error: Throwable ->
            Timber.w(error, "$id: Error when submitting DownloadDiagnosisKeysTask.")

            if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                Timber.w(error, "$id: Retry attempts exceeded.")

                BackgroundWorkHelper.sendDebugNotification(
                    "KeyOneTime Executing: Failure",
                    "KeyOneTime failed with $runAttemptCount attempts"
                )

                return Result.failure()
            } else {
                Timber.d(error, "$id: Retrying.")
                result = Result.retry()
            }
        }

        BackgroundWorkHelper.sendDebugNotification(
            "KeyOneTime Executing: End", "KeyOneTime result: $result "
        )

        Timber.d("$id: doWork() finished with %s", result)
        return result
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<DiagnosisKeyRetrievalOneTimeWorker>
}
