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

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        var result = Result.success()
        taskController.submitBlocking(
            DefaultTaskRequest(
                DownloadDiagnosisKeysTask::class,
                DownloadDiagnosisKeysTask.Arguments(),
                originTag = "DiagnosisKeyRetrievalOneTimeWorker"
            )
        ).error?.also { error: Throwable ->
            Timber.tag(TAG).w(error, "$id: Error when submitting DownloadDiagnosisKeysTask.")

            if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                Timber.tag(TAG).w(error, "$id: Retry attempts exceeded.")

                return Result.failure()
            } else {
                Timber.tag(TAG).d(error, "$id: Retrying.")
                result = Result.retry()
            }
        }

        Timber.tag(TAG).d("$id: doWork() finished with %s", result)
        return result
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<DiagnosisKeyRetrievalOneTimeWorker>

    companion object {
        private val TAG = DiagnosisKeyRetrievalOneTimeWorker::class.java.simpleName
    }
}
