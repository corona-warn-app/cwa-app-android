package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.NEW_MESSAGE_TEST_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeAndDateExtensions
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.stop
import timber.log.Timber

/**
 * Diagnosis Test Result Periodic retrieval
 *
 * @see BackgroundWorkScheduler
 */
class DiagnosisTestResultRetrievalPeriodicWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    /**
     * Work execution
     *
     * If background job is running for less than 21 days, testResult is checked.
     * If the job is running for more than 21 days, the job will be stopped
     *
     * @return Result
     *
     * @see LocalData.isTestResultNotificationSent
     * @see LocalData.initialPollingForTestResultTimeStamp
     */
    override suspend fun doWork(): Result {
        Timber.d("$id: doWork() started. Run attempt: $runAttemptCount")
        BackgroundWorkHelper.sendDebugNotification(
            "TestResult Executing: Start", "TestResult started. Run attempt: $runAttemptCount "
        )

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.d("$id doWork() failed after $runAttemptCount attempts. Rescheduling")

            BackgroundWorkHelper.sendDebugNotification(
                "TestResult Executing: Failure", "TestResult failed with $runAttemptCount attempts"
            )
            BackgroundWorkScheduler.scheduleDiagnosisKeyPeriodicWork()
            Timber.d("$id Rescheduled background worker")

            return Result.failure()
        }
        var result = Result.success()
        try {
            if (TimeAndDateExtensions.calculateDays(
                    LocalData.initialPollingForTestResultTimeStamp(),
                    System.currentTimeMillis()
                ) < BackgroundConstants.POLLING_VALIDITY_MAX_DAYS
            ) {
                Timber.d(" $id maximum days not exceeded")
                val testResult = SubmissionService.asyncRequestTestResult()
                initiateNotification(testResult)
                Timber.d(" $id Test Result Notification Initiated")
            } else {
                stopWorker()
                Timber.d(" $id worker stopped")
            }
        } catch (e: Exception) {
            result = Result.retry()
        }

        BackgroundWorkHelper.sendDebugNotification(
            "TestResult Executing: End", "TestResult result: $result "
        )
        Timber.d("$id: doWork() finished with %s", result)

        return result
    }

    /**
     * Notification Initiation
     *
     * If the returned Test Result is Negative, Positive or Invalid
     * The Background polling  will be stopped
     * and a notification is shown, but only if the App is not in foreground
     *
     * @see LocalData.isTestResultNotificationSent
     * @see LocalData.initialPollingForTestResultTimeStamp
     * @see TestResult
     */
    private fun initiateNotification(testResult: TestResult) {
        if (LocalData.isTestResultNotificationSent() || LocalData.submissionWasSuccessful()) {
            Timber.d("$id: Notification already sent or there was a successful submission")
            return
        }
        Timber.d("$id: Test Result retried is $testResult")
        if (testResult == TestResult.NEGATIVE || testResult == TestResult.POSITIVE ||
            testResult == TestResult.INVALID
        ) {
            NotificationHelper.sendNotificationIfAppIsNotInForeground(
                CoronaWarnApplication.getAppContext().getString(R.string.notification_body),
                NEW_MESSAGE_TEST_RESULT_NOTIFICATION_ID
            )
            NotificationHelper.cancelCurrentNotification(NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID)

            Timber.d("$id: Test Result available - notification issued & risk level notification canceled")
            LocalData.isTestResultNotificationSent(true)
            stopWorker()
        }
    }

    /**
     * Stops the Background Polling worker
     *
     * @see LocalData.initialPollingForTestResultTimeStamp
     * @see BackgroundWorkScheduler.stop
     */
    private fun stopWorker() {
        LocalData.initialPollingForTestResultTimeStamp(0L)
        BackgroundWorkScheduler.WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop()
        Timber.d("$id: Background worker stopped")
        BackgroundWorkHelper.sendDebugNotification(
            "TestResult Stopped", "TestResult Stopped"
        )
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<DiagnosisTestResultRetrievalPeriodicWorker>
}
