package de.rki.coronawarnapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant

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
        if (BuildConfig.DEBUG) Log.d(TAG, "Background job started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            if (BuildConfig.DEBUG) Log.d(
                TAG,
                "Background job failed after $runAttemptCount attempts. Rescheduling"
            )
            return Result.failure()
        }
        var result = Result.success()
        try {
            val currentDate = DateTime(Instant.now(), DateTimeZone.getDefault())
            val lastFetch = DateTime(
                LocalData.lastTimeDiagnosisKeysFromServerFetch(),
                DateTimeZone.getDefault()
            )
            if (LocalData.lastTimeDiagnosisKeysFromServerFetch() == null ||
                currentDate.withTimeAtStartOfDay() != lastFetch.withTimeAtStartOfDay()
            ) {
                RetrieveDiagnosisKeysTransaction.start()
            }
        } catch (e: Exception) {
            result = Result.retry()
        }
        return result
    }
}
