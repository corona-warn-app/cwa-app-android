package de.rki.coronawarnapp.coronatest.notification

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.latestRAT
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareTestResultNotificationService @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val cwaSettings: CWASettings,
    private val coronaTestRepository: CoronaTestRepository,
    private val notification: ShareTestResultNotification
) {

    fun setup() {
        Timber.d("setup()")
        schedulePositiveTestsReminder()
        // if no PCR test is stored or if it was deleted, we reset the reminder
        resetPositivePCRTestReminder()
        // if no RAT test is stored or if it was deleted, we reset the reminder
        resetPositiveRATTestReminder()
    }

    fun maybeShowSharePositiveTestResultNotification(notificationId: Int, testType: CoronaTest.Type) {
        Timber.tag(TAG).d(
            "maybeShowSharePositiveTestResultNotification(notificationId=%s,testType=%s)",
            notificationId,
            testType
        )
        if (testType == PCR) {
            if (cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr > 0) {
                cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr -= 1
                notification.showSharePositiveTestResultNotification(notificationId, testType)
            } else {
                notification.cancelSharePositiveTestResultNotification(testType)
            }
        } else if (testType == RAPID_ANTIGEN) {
            if (cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat > 0) {
                cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat -= 1
                notification.showSharePositiveTestResultNotification(notificationId, testType)
            } else {
                notification.cancelSharePositiveTestResultNotification(testType)
            }
        }
    }

    private fun resetPositivePCRTestReminder() {
        Timber.tag(TAG).v("resetPositivePCRTestReminder")
        coronaTestRepository.latestPCRT // This should not crash ,it is just a flow already constructed
            .onEach {
                if (it == null) {
                    resetSharePositiveTestResultNotification(PCR)
                }
            }
            // Catch error in onEach block
            .catch { it.reportProblem("Failed to reset positive test result reminder for PCR test.") }
            .launchIn(appScope)
    }

    private fun resetPositiveRATTestReminder() {
        Timber.tag(TAG).v("resetPositiveRATTestReminder")
        coronaTestRepository.latestRAT // This should not crash ,it is just a flow already constructed
            .onEach {
                if (it == null) {
                    resetSharePositiveTestResultNotification(RAPID_ANTIGEN)
                }
            }
            // Catch error in onEach block
            .catch { it.reportProblem("Failed to reset positive test result reminder for RAT test.") }
            .launchIn(appScope)
    }

    private fun schedulePositiveTestsReminder() {
        Timber.tag(TAG).v("schedulePositiveTestsReminder")
        coronaTestRepository.coronaTests // This should not crash ,it is just a flow already constructed
            .onEach { tests ->
                // schedule reminder if test wasn't submitted
                tests.filter { test ->
                    test.isSubmissionAllowed && !test.isSubmitted
                }.forEach { test ->
                    maybeScheduleSharePositiveTestResultReminder(test.type)
                }

                // cancel the reminder when test is submitted
                tests.filter { it.isSubmitted }
                    .forEach { notification.cancelSharePositiveTestResultNotification(it.type) }
            }
            // Catch error in onEach block
            .catch { it.reportProblem("Failed to schedule positive test result reminder.") }
            .launchIn(appScope)
    }

    private fun maybeScheduleSharePositiveTestResultReminder(testType: CoronaTest.Type) {
        when (testType) {
            PCR -> {
                if (cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr < 0) {
                    Timber.tag(TAG).v("Schedule positive test result notification for PCR test")
                    cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr =
                        POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
                    notification.scheduleSharePositiveTestResultReminder(testType)
                } else {
                    Timber.tag(TAG).v("Positive test result notification for PCR test has already been scheduled")
                }
            }
            RAPID_ANTIGEN -> {
                if (cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat < 0) {
                    Timber.tag(TAG).v("Schedule positive test result notification for RAT test")
                    cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat =
                        POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
                    notification.scheduleSharePositiveTestResultReminder(testType)
                } else {
                    Timber.tag(TAG).v("Positive test result notification for RAT test has already been scheduled")
                }
            }
        }
    }

    private fun resetSharePositiveTestResultNotification(testType: CoronaTest.Type) {
        Timber.tag(TAG).v("resetSharePositiveTestResultNotification(testType=%s)", testType)
        notification.cancelSharePositiveTestResultNotification(testType)

        when (testType) {
            PCR -> cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr = Int.MIN_VALUE
            RAPID_ANTIGEN -> cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat = Int.MIN_VALUE
        }

        Timber.tag(TAG).v("Share positive test result notification counter has been reset for all test types")
    }

    companion object {
        private val TAG = ShareTestResultNotificationService::class.simpleName
    }
}
