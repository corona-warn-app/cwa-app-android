package de.rki.coronawarnapp.notification

import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_INITIAL_OFFSET
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_INTERVAL
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import javax.inject.Inject

class TestResultNotificationService @Inject constructor(
    @AppContext private val context: Context,
    private val timeStamper: TimeStamper
) {

    fun schedulePositiveTestResultReminder() {
        if (LocalData.numberOfRemainingPositiveTestResultReminders < 0) {
            Timber.v("Schedule positive test result notification")
            LocalData.numberOfRemainingPositiveTestResultReminders = POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
            NotificationHelper.scheduleRepeatingNotification(
                timeStamper.nowUTC.plus(POSITIVE_RESULT_NOTIFICATION_INITIAL_OFFSET),
                POSITIVE_RESULT_NOTIFICATION_INTERVAL,
                POSITIVE_RESULT_NOTIFICATION_ID
            )
        } else {
            Timber.v("Positive test result notification has already been scheduled")
        }
    }

    fun showPositiveTestResultNotification(notificationId: Int) {
        if (LocalData.numberOfRemainingPositiveTestResultReminders > 0) {
            LocalData.numberOfRemainingPositiveTestResultReminders -= 1
            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setComponentName(MainActivity::class.java)
                .setDestination(R.id.submissionResultFragment)
                .createPendingIntent()

            NotificationHelper.sendNotification(
                title = context.getString(R.string.notification_headline_share_positive_result),
                content = context.getString(R.string.notification_body_share_positive_result),
                notificationId = notificationId,
                pendingIntent = pendingIntent
            )
        } else {
            NotificationHelper.cancelFutureNotifications(notificationId)
        }
    }

    fun cancelPositiveTestResultNotification() {
        NotificationHelper.cancelFutureNotifications(POSITIVE_RESULT_NOTIFICATION_ID)
        Timber.v("Future positive test result notifications have been canceled")
    }

    fun resetPositiveTestResultNotification() {
        cancelPositiveTestResultNotification()
        LocalData.numberOfRemainingPositiveTestResultReminders = Int.MIN_VALUE
        Timber.v("Positive test result notification counter has been reset")
    }
}
