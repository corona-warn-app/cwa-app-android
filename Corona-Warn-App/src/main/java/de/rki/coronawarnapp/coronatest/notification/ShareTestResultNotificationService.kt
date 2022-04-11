package de.rki.coronawarnapp.coronatest.notification

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.latestRAT
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_PCR_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RAT_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("MaxLineLength")
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

    fun maybeShowSharePositiveTestResultNotification(
        notificationId: Int,
        testType: BaseCoronaTest.Type,
        testIdentifier: TestIdentifier
    ) {
        Timber.tag(TAG).d(
            "maybeShowSharePositiveTestResultNotification(notificationId=%s, testType=%s, testIdentifier=%s)",
            notificationId,
            testType,
            testIdentifier
        )
        if (testType == PCR) {
            if (cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr > 0) {
                cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr -= 1
                notification.showSharePositiveTestResultNotification(notificationId, testIdentifier)
            } else {
                notification.cancelSharePositiveTestResultNotification(testType, POSITIVE_PCR_RESULT_NOTIFICATION_ID)
            }
        } else if (testType == RAPID_ANTIGEN) {
            if (cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat > 0) {
                cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat -= 1
                notification.showSharePositiveTestResultNotification(notificationId, testIdentifier)
            } else {
                notification.cancelSharePositiveTestResultNotification(testType, POSITIVE_RAT_RESULT_NOTIFICATION_ID)
            }
        }
    }

    private fun resetPositivePCRTestReminder() {
        Timber.tag(TAG).v("resetPositivePCRTestReminder")
        coronaTestRepository.latestPCRT // This should not crash ,it is just a flow already constructed
            .onEach {
                if (it == null) {
                    resetSharePositiveTestResultNotification(PCR, POSITIVE_PCR_RESULT_NOTIFICATION_ID)
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
                    resetSharePositiveTestResultNotification(RAPID_ANTIGEN, POSITIVE_RAT_RESULT_NOTIFICATION_ID)
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
                    test.isSubmissionAllowed && !test.isSubmitted && test.isViewed
                }.forEach { test ->
                    maybeScheduleSharePositiveTestResultReminder(test.type, test.identifier)
                }

                // cancel the reminder when test is submitted
                tests.filter { it.isSubmitted }
                    .forEach {
                        notification.cancelSharePositiveTestResultNotification(
                            it.type,
                            if (it.type == RAPID_ANTIGEN)
                                POSITIVE_RAT_RESULT_NOTIFICATION_ID
                            else
                                POSITIVE_PCR_RESULT_NOTIFICATION_ID
                        )
                    }
            }
            // Catch error in onEach block
            .catch { it.reportProblem("Failed to schedule positive test result reminder.") }
            .launchIn(appScope)
    }

    private fun maybeScheduleSharePositiveTestResultReminder(testType: BaseCoronaTest.Type, testId: TestIdentifier) {
        when (testType) {
            PCR -> {
                if (cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr < 0 ||
                    (
                        cwaSettings.idOfPositiveTestResultRemindersPcr != null &&
                            testId != cwaSettings.idOfPositiveTestResultRemindersPcr
                        )
                ) {
                    Timber.tag(TAG)
                        .v("Schedule positive test result notification for PCR test reminders = ${cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr}, testId == $testId, previous testId = ${cwaSettings.idOfPositiveTestResultRemindersPcr}")
                    cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr =
                        POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
                    cwaSettings.idOfPositiveTestResultRemindersPcr = testId
                    notification.scheduleSharePositiveTestResultReminder(
                        testType,
                        testId,
                        POSITIVE_PCR_RESULT_NOTIFICATION_ID
                    )
                } else {
                    Timber.tag(TAG)
                        .v("Positive test result notification for PCR test has already been scheduled reminders = ${cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr}, testId == $testId, previous testId = ${cwaSettings.idOfPositiveTestResultRemindersPcr}")
                }
            }
            RAPID_ANTIGEN -> {
                if (cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat < 0 ||
                    (
                        cwaSettings.idOfPositiveTestResultRemindersRat != null &&
                            testId != cwaSettings.idOfPositiveTestResultRemindersRat
                        )
                ) {
                    Timber.tag(TAG)
                        .v("Schedule positive test result notification for RAT test reminders = ${cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat}, testId == $testId, previous testId = ${cwaSettings.idOfPositiveTestResultRemindersRat}")
                    cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat =
                        POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
                    cwaSettings.idOfPositiveTestResultRemindersRat = testId
                    notification.scheduleSharePositiveTestResultReminder(
                        testType,
                        testId,
                        POSITIVE_RAT_RESULT_NOTIFICATION_ID
                    )
                } else {
                    Timber.tag(TAG)
                        .v("Positive test result notification for RAT test has already been scheduled reminders = ${cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat}, testId == $testId, previous testId = ${cwaSettings.idOfPositiveTestResultRemindersRat}")
                }
            }
        }
    }

    private fun resetSharePositiveTestResultNotification(testType: BaseCoronaTest.Type, notificationId: Int) {
        Timber.tag(TAG).v("resetSharePositiveTestResultNotification(testType=%s)", testType)
        notification.cancelSharePositiveTestResultNotification(testType, notificationId)

        when (testType) {
            PCR -> cwaSettings.numberOfRemainingSharePositiveTestResultRemindersPcr = Int.MIN_VALUE
            RAPID_ANTIGEN -> cwaSettings.numberOfRemainingSharePositiveTestResultRemindersRat = Int.MIN_VALUE
        }

        Timber.tag(TAG).v("Share positive test result notification counter has been reset for $testType test")
    }

    companion object {
        private val TAG = tag<ShareTestResultNotificationService>()
    }
}
