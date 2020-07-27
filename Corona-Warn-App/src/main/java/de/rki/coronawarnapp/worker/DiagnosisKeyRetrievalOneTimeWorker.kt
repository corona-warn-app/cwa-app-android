package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
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
        BackgroundWorkHelper.sendDebugNotification(
            "KeyOneTime Executing: Start", "KeyOneTime started. Run attempt: $runAttemptCount ")

        var result = Result.success()
        try {
            val currentDate = DateTime(Instant.now(), DateTimeZone.UTC)
            val lastFetch = DateTime(
                LocalData.lastTimeDiagnosisKeysFromServerFetch(),
                DateTimeZone.UTC
            )
            if (LocalData.lastTimeDiagnosisKeysFromServerFetch() == null ||
                currentDate.withTimeAtStartOfDay() != lastFetch.withTimeAtStartOfDay()
            ) {
                RetrieveDiagnosisKeysTransaction.start()
            }
        } catch (e: Exception) {
            if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {

                BackgroundWorkHelper.sendDebugNotification(
                    "KeyOneTime Executing: Failure", "KeyOneTime failed with $runAttemptCount attempts")

                return Result.failure()
            } else {
                result = Result.retry()
            }
        }

        BackgroundWorkHelper.sendDebugNotification(
            "KeyOneTime Executing: End", "KeyOneTime result: $result ")

        return result
    }
}
