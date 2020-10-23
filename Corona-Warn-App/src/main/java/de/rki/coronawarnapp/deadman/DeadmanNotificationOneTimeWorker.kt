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
 * @see BackgroundWorkScheduler
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
        BackgroundWorkHelper.sendDebugNotification(
            "Deadman Notification Executing: Start", "Deadman Notification started. Run attempt: $runAttemptCount ")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.d("Background job failed after $runAttemptCount attempts. Rescheduling")

            BackgroundWorkHelper.sendDebugNotification(
                "Deadman Notification Executing: Failure", "Deadman Notification failed with $runAttemptCount attempts")

//            BackgroundWorkScheduler.scheduleDiagnosisKeyPeriodicWork()
            return Result.failure()
        }
        var result = Result.success()
        try {
            if (!CoronaWarnApplication.isAppInForeground) {
                NotificationHelper.sendNotification(
                    CoronaWarnApplication.getAppContext()
                        .getString(R.string.risk_details_deadman_notification_title), CoronaWarnApplication.getAppContext()
                        .getString(R.string.risk_details_deadman_notification_body),
                    NotificationCompat.PRIORITY_HIGH
                )
            }
        } catch (e: Exception) {
            result = Result.retry()
        }

        BackgroundWorkHelper.sendDebugNotification(
            "Deadman Notification Executing: End", "TestResult result: $result ")

        return result
    }
}
