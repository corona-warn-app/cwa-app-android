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
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.stop
import kotlinx.coroutines.flow.first
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
    private val submissionRepository: SubmissionRepository
) : CoroutineWorker(context, workerParams) {

    /**
     * If background job is running for less than 21 days, testResult is checked.
     * If the job is running for more than 21 days, the job will be stopped
     *
     * @see LocalData.isTestResultNotificationAvailableSent
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
            }

            Timber.tag(TAG).d(" $id Running worker.")
            val registrationToken = LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
            val testResult = submissionRepository.asyncRequestTestResult(registrationToken)
            initiateTestResultAvailableNotification(testResult)
        } catch (e: Exception) {
            result = Result.retry()
        }

        Timber.tag(TAG).d("$id: doWork() finished with %s", result)

        return result
    }

    private suspend fun abortConditionsMet(): Boolean {
        if (LocalData.isTestResultNotificationAvailableSent()) {
            Timber.tag(TAG).d("$id: Notification already sent.")
            return true
        }
        if (submissionRepository.hasViewedTestResult.first()) {
            Timber.tag(TAG).d("$id: Test result has already been viewed.")
            return true
        }

        if (TimeAndDateExtensions.calculateDays(
                LocalData.initialPollingForTestResultTimeStamp(),
                System.currentTimeMillis()
            ) >= BackgroundConstants.POLLING_VALIDITY_MAX_DAYS
        ) {
            Timber.tag(TAG)
                .d(" $id Maximum days of ${BackgroundConstants.POLLING_VALIDITY_MAX_DAYS} days for polling exceeded.")
            return true
        }

        return false
    }

    /**
     * Notification Initiation
     *
     * If the returned Test Result is Negative, Positive or Invalid
     * The Background polling  will be stopped
     * and a notification is shown, but only if the App is not in foreground
     *
     * @see LocalData.isTestResultNotificationAvailableSent
     * @see LocalData.initialPollingForTestResultTimeStamp
     * @see TestResult
     */
    private suspend fun initiateTestResultAvailableNotification(testResult: TestResult) {

        Timber.tag(TAG).d("$id: Test Result retrieved is $testResult")
        if (testResult == TestResult.NEGATIVE || testResult == TestResult.POSITIVE ||
            testResult == TestResult.INVALID
        ) {
            testResultAvailableNotificationService.showTestResultAvailableNotification(testResult)

            notificationHelper.cancelCurrentNotification(
                NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
            )

            Timber.tag(TAG).d("$id: Test Result available - notification issued & risk level notification canceled")
            LocalData.isTestResultNotificationAvailableSent(true)
            stopWorker()
        }
        Timber.tag(TAG).d(" $id Test Result Notification Initiated")
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
