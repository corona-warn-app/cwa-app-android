package de.rki.coronawarnapp.deadman

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.ui.main.MainActivity
import kotlin.random.Random

class DeadmanNotificationSender {

    /**
     * Notification channel id
     *
     * @see NotificationConstants.NOTIFICATION_CHANNEL_ID
     */
    private val channelId =
        CoronaWarnApplication.getAppContext()
            .getString(NotificationConstants.NOTIFICATION_CHANNEL_ID)

    /**
     * Create pending intent to main activity
     */
    private fun createPendingIntentToMainActivity() =
        PendingIntent.getActivity(
            CoronaWarnApplication.getAppContext(),
            0,
            Intent(CoronaWarnApplication.getAppContext(), MainActivity::class.java),
            0
        )

    /**
     * Build notification
     * Create notification with defined title, content text and visibility.
     *
     * @param title: String
     * @param content: String
     *
     * @see NotificationCompat.VISIBILITY_PUBLIC
     * @see NotificationCompat.PRIORITY_MAX
     */
    private fun buildNotification(
        title: String,
        content: String
    ): Notification? {
        val builder = NotificationCompat.Builder(CoronaWarnApplication.getAppContext(),
            channelId
        )
            .setSmallIcon(NotificationConstants.NOTIFICATION_SMALL_ICON)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createPendingIntentToMainActivity())
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(content)

        return builder.build()
    }

    /**
     * Build and send notification with predefined title and content.
     */
    fun sendNotification() {
        if (CoronaWarnApplication.isAppInForeground) {
            return
        }
        val title =  CoronaWarnApplication.getAppContext()
            .getString(R.string.risk_details_deadman_notification_title)
        val content = CoronaWarnApplication.getAppContext()
            .getString(R.string.risk_details_deadman_notification_body)
        val notification =
            buildNotification(title, content) ?: return
        with(NotificationManagerCompat.from(CoronaWarnApplication.getAppContext())) {
            notify(Random.nextInt(), notification)
        }
    }
}
