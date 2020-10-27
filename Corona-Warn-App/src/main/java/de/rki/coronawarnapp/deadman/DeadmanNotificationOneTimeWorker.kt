package de.rki.coronawarnapp.deadman

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.worker.BackgroundConstants
import de.rki.coronawarnapp.worker.BackgroundWorkHelper
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import timber.log.Timber

/**
 * One time background deadman notification worker
 *
 * @see DeadmanNotificationScheduler
 */
class DeadmanNotificationOneTimeWorker(
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
            DeadmanNotificationSender().sendNotification()
        } catch (e: Exception) {
            result = Result.retry()
        }

        return result
    }
}
