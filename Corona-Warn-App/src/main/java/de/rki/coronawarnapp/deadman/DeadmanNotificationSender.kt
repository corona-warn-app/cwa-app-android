package de.rki.coronawarnapp.deadman

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Reusable
class DeadmanNotificationSender @Inject constructor(
    @AppContext private val context: Context,
    private val foregroundState: ForegroundState,
    private val notificationManagerCompat: NotificationManagerCompat
) {

    private val channelId =
        context.getString(NotificationConstants.NOTIFICATION_CHANNEL_ID)

    private fun createPendingIntentToMainActivity() =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            0
        )

    private fun buildNotification(
        title: String,
        content: String
    ): Notification? {
        val builder = NotificationCompat.Builder(context,
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

    suspend fun sendNotification() {
        if (foregroundState.isInForeground.first()) {
            return
        }
        val title = context.getString(R.string.risk_details_deadman_notification_title)
        val content = context.getString(R.string.risk_details_deadman_notification_body)
        val notification =
            buildNotification(title, content) ?: return
        with(notificationManagerCompat) {
            notify(DEADMAN_NOTIFICATION_ID, notification)
        }
    }

    companion object {
        /**
         * Deadman notification id
         */
        const val DEADMAN_NOTIFICATION_ID = 3
    }
}
