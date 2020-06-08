package de.rki.coronawarnapp.worker

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeAndDateExtensions
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.stop

/**
 * Diagnosis Test Result Periodic retrieavl
 *
 * @see BackgroundWorkScheduler
 */
class DiagnosisTestResultRetrievalPeriodicWorker(
    val context: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG: String? = DiagnosisTestResultRetrievalPeriodicWorker::class.simpleName
    }

    /**
     * Work execution
     *
     * @return Result
     *
     * @see LocalData.initialTestResultNotification
     * @see LocalData.initialPollingForTestResultTimeStamp
     */
    override suspend fun doWork(): Result {

        if (BuildConfig.DEBUG) Log.d(
            TAG,
            "Background job started. Run attempt: $runAttemptCount"
        )

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            if (BuildConfig.DEBUG) Log.d(
                TAG,
                "Background job failed after $runAttemptCount attempts. Rescheduling"
            )
            BackgroundWorkScheduler.scheduleDiagnosisKeyPeriodicWork()
            return Result.failure()
        }
        var result = Result.success()
        try {
            if (TimeAndDateExtensions.calculateDays(
                    LocalData.initialPollingForTestResultTimeStamp(),
                    System.currentTimeMillis()
                ) < BackgroundConstants.POLLING_VALIDITY_MAX_DAYS
            ) {
                val testResult = SubmissionService.asyncRequestTestResult()
                initiateNotification(testResult)
            }
        } catch (e: Exception) {
            result = Result.retry()
        }
        return result
    }

    private fun initiateNotification(testResult: TestResult) {
        if (testResult == TestResult.NEGATIVE || testResult == TestResult.POSITIVE) {
            if (!CoronaWarnApplication.isAppInForeground) {
                LocalData.initialTestResultNotification(true)
                NotificationHelper.sendNotification(
                    CoronaWarnApplication.getAppContext()
                        .getString(R.string.notification_name), CoronaWarnApplication.getAppContext()
                        .getString(R.string.notification_body),
                    NotificationCompat.PRIORITY_HIGH
                )
            }
            stopWorker()
        } else {
            stopWorker()
        }
    }

    private fun stopWorker() {
        LocalData.initialPollingForTestResultTimeStamp(0L)
        BackgroundWorkScheduler.WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop()
    }
}
