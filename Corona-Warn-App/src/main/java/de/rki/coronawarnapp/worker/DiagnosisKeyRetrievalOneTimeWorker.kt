package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import timber.log.Timber

/**
 * One time diagnosis key retrieval work
 * Executes the retrieve diagnosis key transaction
 *
 * @see BackgroundWorkScheduler
 */
class DiagnosisKeyRetrievalOneTimeWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG: String? = DiagnosisKeyRetrievalOneTimeWorker::class.simpleName
    }

    /**
     * Work execution
     *
     * @return Result
     *
     * @see RetrieveDiagnosisKeysTransaction
     */
    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        BackgroundWorkHelper.sendDebugNotification(
            "KeyOneTime Executing: Start", "KeyOneTime started. Run attempt: $runAttemptCount "
        )

        var result = Result.success()
        try {
            RetrieveDiagnosisKeysTransaction.startWithConstraints()
        } catch (e: Exception) {
            Timber.tag(TAG).w(
                e, "$id: Error during RetrieveDiagnosisKeysTransaction.startWithConstraints()."
            )

            if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                Timber.tag(TAG).w(e, "$id: Retry attempts exceeded.")

                BackgroundWorkHelper.sendDebugNotification(
                    "KeyOneTime Executing: Failure",
                    "KeyOneTime failed with $runAttemptCount attempts"
                )

                return Result.failure()
            } else {
                Timber.tag(TAG).d(e, "$id: Retrying.")
                result = Result.retry()
            }
        }

        BackgroundWorkHelper.sendDebugNotification(
            "KeyOneTime Executing: End", "KeyOneTime result: $result "
        )

        Timber.tag(TAG).d("$id: doWork() finished with %s", result)
        return result
    }
}
