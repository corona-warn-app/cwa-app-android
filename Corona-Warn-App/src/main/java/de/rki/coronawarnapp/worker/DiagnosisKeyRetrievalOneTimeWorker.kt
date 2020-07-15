package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
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
class DiagnosisKeyRetrievalOneTimeWorker @WorkerInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    val retrieveDiagnosisKeysTransaction: RetrieveDiagnosisKeysTransaction
) : CoroutineWorker(context, workerParams) {

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
            val currentDate = DateTime(Instant.now(), DateTimeZone.UTC)
            val lastFetch = DateTime(
                LocalData.lastTimeDiagnosisKeysFromServerFetch(),
                DateTimeZone.UTC
            )
            if (LocalData.lastTimeDiagnosisKeysFromServerFetch() == null ||
                currentDate.withTimeAtStartOfDay() != lastFetch.withTimeAtStartOfDay()
            ) {
                retrieveDiagnosisKeysTransaction.start()
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
