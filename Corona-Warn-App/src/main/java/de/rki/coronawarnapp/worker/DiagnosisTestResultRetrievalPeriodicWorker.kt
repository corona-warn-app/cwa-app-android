package de.rki.coronawarnapp.worker

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.report
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.util.TimeAndDateExtensions
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.stop

/**
 * One time diagnosis key retrieval work
 * Executes the retrieve diagnosis key transaction
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

        if (BuildConfig.DEBUG) Log.d(TAG, "Background polling job started...")

        try {
            if (TimeAndDateExtensions.calculateDays(
                    LocalData.initialPollingForTestResultTimeStamp(),
                    System.currentTimeMillis()
                ) < BackgroundConstants.POLLING_VALIDITY_MAX_DAYS
            ) {
                val testResult = SubmissionService.asyncRequestTestResult()
                if (testResult == TestResult.NEGATIVE || testResult == TestResult.POSITIVE) {
                    if (!CoronaWarnApplication.isAppInForeground) {
                        if (!LocalData.initialTestResultNotification()) {
                            LocalData.initialTestResultNotification(true)
                            NotificationHelper.sendNotification(
                                CoronaWarnApplication.getAppContext()
                                    .getString(R.string.notification_name), CoronaWarnApplication.getAppContext()
                                    .getString(R.string.notification_body),
                                NotificationCompat.PRIORITY_HIGH
                            )
                        }
                    }
                    stopWorker()
                }
            } else {
                stopWorker()
            }
        } catch (e: Exception) {
            e.report(ExceptionCategory.JOB)
            return Result.failure()
        }
        return Result.success()
    }

    private fun stopWorker() {
        LocalData.initialPollingForTestResultTimeStamp(0L)
        BackgroundWorkScheduler.WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop()
    }
}
