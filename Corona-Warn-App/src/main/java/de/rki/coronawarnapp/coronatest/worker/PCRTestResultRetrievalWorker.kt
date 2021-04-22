package de.rki.coronawarnapp.coronatest.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.execution.TestResultScheduler
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber

/**
 * Diagnosis test result retrieval by periodic polling
 */
class PCRTestResultRetrievalWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val testResultAvailableNotificationService: PCRTestResultAvailableNotificationService,
    private val notificationHelper: GeneralNotifications,
    private val coronaTestRepository: CoronaTestRepository,
    private val timeStamper: TimeStamper,
    private val testResultScheduler: TestResultScheduler,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.tag(TAG).d("$id doWork() failed after $runAttemptCount attempts. Rescheduling")

            testResultScheduler.setPcrPeriodicTestPolling(enabled = true)
            Timber.tag(TAG).d("$id Rescheduled background worker")

            return Result.failure()
        }
        var result = Result.success()
        try {
            if (abortConditionsMet(timeStamper.nowUTC)) {
                Timber.tag(TAG).d(" $id Stopping worker.")
                stopWorker()
            } else {
                Timber.tag(TAG).d(" $id Running worker.")

                val coronaTest = coronaTestRepository.refresh(
                    type = CoronaTest.Type.PCR
                ).single() as PCRCoronaTest
                val testResult = coronaTest.testResult

                Timber.tag(TAG).d("$id: Test Result retrieved is $testResult")

                if (testResult == CoronaTestResult.PCR_NEGATIVE ||
                    testResult == CoronaTestResult.PCR_POSITIVE ||
                    testResult == CoronaTestResult.PCR_INVALID
                ) {
                    sendTestResultAvailableNotification(coronaTest)
                    cancelRiskLevelScoreNotification()
                    Timber.tag(TAG)
                        .d("$id: Test Result available - notification sent & risk level notification canceled")
                    stopWorker()
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Test result retrieval worker failed.")
            result = Result.retry()
        }

        Timber.tag(TAG).d("$id: doWork() finished with %s", result)

        return result
    }

    private suspend fun abortConditionsMet(nowUTC: Instant): Boolean {
        val pcrTest = coronaTestRepository.latestPCRT.first()
        if (pcrTest == null) {
            Timber.tag(TAG).w("There is no PCR test available!?")
            return true
        }

        if (pcrTest.isResultAvailableNotificationSent) {
            Timber.tag(TAG).d("$id: Notification already sent.")
            return true
        }

        if (pcrTest.isViewed) {
            Timber.tag(TAG).d("$id: Test result has already been viewed.")
            return true
        }

        val calculateDays = Duration(pcrTest.registeredAt, nowUTC).standardDays
        Timber.tag(TAG).d("Calculated days: %d", calculateDays)

        if (calculateDays >= BackgroundConstants.POLLING_VALIDITY_MAX_DAYS) {
            Timber.tag(TAG).d("$id $calculateDays is exceeding the maximum polling duration")
            return true
        }

        return false
    }

    private suspend fun sendTestResultAvailableNotification(coronaTest: CoronaTest) {
        testResultAvailableNotificationService.showTestResultAvailableNotification(coronaTest.testResult)
        coronaTestRepository.updateResultNotification(identifier = coronaTest.identifier, sent = true)
    }

    private fun cancelRiskLevelScoreNotification() {
        notificationHelper.cancelCurrentNotification(
            NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
        )
    }

    private fun stopWorker() {
        testResultScheduler.setPcrPeriodicTestPolling(enabled = false)
        Timber.tag(TAG).d("$id: Background worker stopped")
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<PCRTestResultRetrievalWorker>

    companion object {
        private val TAG = PCRTestResultRetrievalWorker::class.java.simpleName
    }
}
