package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.TimeAndDateExtensions
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
        Timber.d("Background job started. Run attempt: $runAttemptCount ")

        var result = Result.success()
        try {
            val keysWereNotRetrievedToday = TimeAndDateExtensions.calculateIfCurrentTimeIsNewDay(
                LocalData.lastTimeDiagnosisKeysFromServerFetch())
            if (keysWereNotRetrievedToday) {
                RetrieveDiagnosisKeysTransaction.start()
            }
        } catch (e: Exception) {
            if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                return Result.failure()
            } else {
                result = Result.retry()
            }
        }
        return result
    }
}
