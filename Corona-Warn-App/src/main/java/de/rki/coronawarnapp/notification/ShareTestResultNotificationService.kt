package de.rki.coronawarnapp.notification

import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_INITIAL_OFFSET
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_INTERVAL
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import timber.log.Timber
import javax.inject.Inject

class ShareTestResultNotificationService @Inject constructor(
    @AppContext private val context: Context,
    private val timeStamper: TimeStamper,
    private val notificationHelper: GeneralNotifications,
    private val cwaSettings: CWASettings
) {

    fun scheduleSharePositiveTestResultReminder() {
        if (cwaSettings.numberOfRemainingSharePositiveTestResultReminders < 0) {
            Timber.v("Schedule positive test result notification")
            cwaSettings.numberOfRemainingSharePositiveTestResultReminders = POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT
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
        if (cwaSettings.numberOfRemainingSharePositiveTestResultReminders > 0) {
            cwaSettings.numberOfRemainingSharePositiveTestResultReminders -= 1

            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setComponentName(MainActivity::class.java)
                .setDestination(R.id.submissionTestResultAvailableFragment)
                .createPendingIntent()

            val notification = notificationHelper.newBaseBuilder().apply {
                setContentTitle(context.getString(R.string.notification_headline_share_positive_result))
                setContentTextExpandable(context.getString(R.string.notification_body_share_positive_result))
                setContentIntent(pendingIntent)
            }.build()

            notificationHelper.sendNotification(
                notificationId = notificationId,
                notification = notification,
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
        cwaSettings.numberOfRemainingSharePositiveTestResultReminders = Int.MIN_VALUE
        Timber.v("Positive test result notification counter has been reset")
    }
}
