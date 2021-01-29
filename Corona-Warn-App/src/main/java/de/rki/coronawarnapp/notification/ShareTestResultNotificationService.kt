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

class ShareTestResultNotificationService @Inject constructor(
    @AppContext private val context: Context,
    private val timeStamper: TimeStamper,
    private val notificationHelper: NotificationHelper
) {

    fun scheduleSharePositiveTestResultReminder() {
        if (LocalData.numberOfRemainingSharePositiveTestResultReminders < 0) {
            Timber.v("Schedule positive test result notification")
            LocalData.numberOfRemainingSharePositiveTestResultReminders = POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
            notificationHelper.scheduleRepeatingNotification(
                timeStamper.nowUTC.plus(POSITIVE_RESULT_NOTIFICATION_INITIAL_OFFSET),
                POSITIVE_RESULT_NOTIFICATION_INTERVAL,
                POSITIVE_RESULT_NOTIFICATION_ID
            )
        } else {
            Timber.v("Positive test result notification has already been scheduled")
        }
    }

    fun showSharePositiveTestResultNotification(notificationId: Int) {
        if (LocalData.numberOfRemainingSharePositiveTestResultReminders > 0) {
            LocalData.numberOfRemainingSharePositiveTestResultReminders -= 1
            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setComponentName(MainActivity::class.java)
                .setDestination(R.id.submissionTestResultAvailableFragment)
                .createPendingIntent()

            notificationHelper.sendNotification(
                title = context.getString(R.string.notification_headline_share_positive_result),
                content = context.getString(R.string.notification_body_share_positive_result),
                notificationId = notificationId,
                pendingIntent = pendingIntent
            )
        } else {
            notificationHelper.cancelFutureNotifications(notificationId)
        }
    }

    fun cancelSharePositiveTestResultNotification() {
        notificationHelper.cancelFutureNotifications(POSITIVE_RESULT_NOTIFICATION_ID)
        Timber.v("Future positive test result notifications have been canceled")
    }

    fun resetSharePositiveTestResultNotification() {
        cancelSharePositiveTestResultNotification()
        LocalData.numberOfRemainingSharePositiveTestResultReminders = Int.MIN_VALUE
        Timber.v("Positive test result notification counter has been reset")
    }
}
