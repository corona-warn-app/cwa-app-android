package de.rki.coronawarnapp.coronatest.notification

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
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
        coronaTestRepository.coronaTests
            .onEach { tests ->
                when {
                    tests.any { it.isSubmissionAllowed && !it.isSubmitted } -> {
                        maybeScheduleSharePositiveTestResultReminder()
                    }
                    tests.isNotEmpty() -> {
                        notification.cancelSharePositiveTestResultNotification()
                    }
                    tests.isEmpty() -> {
                        resetSharePositiveTestResultNotification()
                    }
                }
            }
            .catch { Timber.e(it, "Failed to schedule positive test result reminder.") }
            .launchIn(appScope)
    }

    fun maybeShowSharePositiveTestResultNotification(notificationId: Int) {
        Timber.d("maybeShowSharePositiveTestResultNotification(notificationId=$notificationId)")
        if (cwaSettings.numberOfRemainingSharePositiveTestResultReminders > 0) {
            cwaSettings.numberOfRemainingSharePositiveTestResultReminders -= 1
            notification.showSharePositiveTestResultNotification(notificationId)
        } else {
            notification.cancelSharePositiveTestResultNotification()
        }
    }

    private fun maybeScheduleSharePositiveTestResultReminder() {
        if (cwaSettings.numberOfRemainingSharePositiveTestResultReminders < 0) {
            Timber.v("Schedule positive test result notification")
            cwaSettings.numberOfRemainingSharePositiveTestResultReminders = POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
            notification.scheduleSharePositiveTestResultReminder()
        } else {
            Timber.v("Positive test result notification has already been scheduled")
        }
    }

    private fun resetSharePositiveTestResultNotification() {
        notification.cancelSharePositiveTestResultNotification()
        cwaSettings.numberOfRemainingSharePositiveTestResultReminders = Int.MIN_VALUE
        Timber.v("Positive test result notification counter has been reset")
    }
}
