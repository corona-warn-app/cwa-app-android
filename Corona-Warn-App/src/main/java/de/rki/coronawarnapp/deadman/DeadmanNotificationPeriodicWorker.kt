package de.rki.coronawarnapp.deadman

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber

/**
 * Periodic background deadman notification worker
 *
 * @see DeadmanNotificationScheduler
 */
class DeadmanNotificationPeriodicWorker(
    val context: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(context, workerParams) {

    /**
     * Work execution
     *
     * @return Result
     */
    override suspend fun doWork(): Result {
        Timber.d("Background job started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.d("Background job failed after $runAttemptCount attempts. Rescheduling")

            return Result.failure()
        }
        var result = Result.success()
        try {
            // Reschedult one time deadman notification work
            DeadmanNotificationScheduler().scheduleOneTime(null);
        } catch (e: Exception) {
            result = Result.retry()
        }

        return result
    }
}
