package de.rki.coronawarnapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.R

import de.rki.coronawarnapp.notification.NotificationConstants.NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.main.MainActivity
import timber.log.Timber

typealias NotificationId = Int

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (val requestCode = intent.getIntExtra(NOTIFICATION_ID, Int.MIN_VALUE)) {
            POSITIVE_RESULT_NOTIFICATION_ID ->
                showPositiveResultNotification(context, requestCode)
            else ->
                Timber.tag(TAG).v("NotificationReceiver received an undefined request code: %s", requestCode)
        }
    }

    private fun showPositiveResultNotification(context: Context, notificationId: Int) {
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

    companion object {
        private val TAG: String? = NotificationReceiver::class.simpleName
    }
}
