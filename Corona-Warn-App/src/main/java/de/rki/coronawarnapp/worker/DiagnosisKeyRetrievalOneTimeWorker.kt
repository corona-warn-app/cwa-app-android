package de.rki.coronawarnapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction

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
    @Suppress("ReturnCount")
    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) Log.d(TAG, "Background job started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Background job failed after $runAttemptCount attempts. Rescheduling")
            return Result.failure()
        }
        try {
            RetrieveDiagnosisKeysTransaction.start()
        } catch (e: Exception) {
            return Result.retry()
        }
        return Result.success()
    }
}
