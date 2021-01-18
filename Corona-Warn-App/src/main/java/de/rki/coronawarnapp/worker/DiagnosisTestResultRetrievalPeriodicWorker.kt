package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.notification.TestResultAvailableNotificationService
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.SubmissionSettings
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
    @Assisted workerParams: WorkerParameters,
    private val testResultAvailableNotificationService: TestResultAvailableNotificationService,
    private val notificationHelper: NotificationHelper,
    private val submissionSettings: SubmissionSettings,
    private val submissionService: SubmissionService
) : CoroutineWorker(context, workerParams) {

    /**
     * If background job is running for less than 21 days, testResult is checked.
     * If the job is running for more than 21 days, the job will be stopped
     *
     * @see LocalData.isTestResultAvailableNotificationSent
     * @see LocalData.initialPollingForTestResultTimeStamp
     */
    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.tag(TAG).d("$id doWork() failed after $runAttemptCount attempts. Rescheduling")

            BackgroundWorkScheduler.scheduleDiagnosisKeyPeriodicWork()
            Timber.tag(TAG).d("$id Rescheduled background worker")

            return Result.failure()
        }
        var result = Result.success()
        try {

            if (abortConditionsMet()) {
                Timber.tag(TAG).d(" $id Stopping worker.")
                stopWorker()
            } else {
                Timber.tag(TAG).d(" $id Running worker.")

                val registrationToken = LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
                val testResult = submissionService.asyncRequestTestResult(registrationToken)
                Timber.tag(TAG).d("$id: Test Result retrieved is $testResult")

                if (testResult == TestResult.NEGATIVE ||
                    testResult == TestResult.POSITIVE ||
                    testResult == TestResult.INVALID
                ) {
                    sendTestResultAvailableNotification(testResult)
                    cancelRiskLevelScoreNotification()
                    Timber.tag(TAG)
                        .d("$id: Test Result available - notification sent & risk level notification canceled")
                    stopWorker()
                }
            }
        } catch (e: Exception) {
            result = Result.retry()
        }

        Timber.tag(TAG).d("$id: doWork() finished with %s", result)

        return result
    }

    private fun abortConditionsMet(): Boolean {
        if (LocalData.isTestResultAvailableNotificationSent()) {
            Timber.tag(TAG).d("$id: Notification already sent.")
            return true
        }
        if (submissionSettings.hasViewedTestResult.value) {
            Timber.tag(TAG).d("$id: Test result has already been viewed.")
            return true
        }

        if (TimeAndDateExtensions.calculateDays(
                LocalData.initialPollingForTestResultTimeStamp(),
                System.currentTimeMillis()
            ) >= BackgroundConstants.POLLING_VALIDITY_MAX_DAYS
        ) {
            Timber.tag(TAG)
                .d(" $id Maximum of ${BackgroundConstants.POLLING_VALIDITY_MAX_DAYS} days for polling exceeded.")
            return true
        }

        return false
    }

    private suspend fun sendTestResultAvailableNotification(testResult: TestResult) {
        testResultAvailableNotificationService.showTestResultAvailableNotification(testResult)
        LocalData.isTestResultAvailableNotificationSent(true)
    }

    private fun cancelRiskLevelScoreNotification() {
        notificationHelper.cancelCurrentNotification(
            NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
        )
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
        Timber.tag(TAG).d("$id: Background worker stopped")
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<DiagnosisTestResultRetrievalPeriodicWorker>

    companion object {
        private val TAG = DiagnosisTestResultRetrievalPeriodicWorker::class.java.simpleName
    }
}
