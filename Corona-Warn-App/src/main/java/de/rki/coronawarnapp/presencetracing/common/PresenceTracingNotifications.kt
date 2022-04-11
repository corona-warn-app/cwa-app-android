package de.rki.coronawarnapp.presencetracing.common

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.hasAPILevel
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import timber.log.Timber
import javax.inject.Inject

/**
 * Helper to send notifications on the notification channel for presence tracing related events.
 *
 * Also see **[de.rki.coronawarnapp.notification.GeneralNotifications]**
 */
@Reusable
class PresenceTracingNotifications @Inject constructor(
    @AppContext private val context: Context,
    private val notificationManagerCompat: NotificationManagerCompat,
    private val navDeepLinkBuilderFactory: NavDeepLinkBuilderFactory,
) {

    private val channelId = "${context.packageName}.notification.presenceTracingChannelId"
    private var isNotificationChannelSetup = false

    fun setupChannel() {
        Timber.d("setupChannel()")

        val channel = NotificationChannelCompat.Builder(
            channelId,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setName(context.getString(R.string.tracelocation_notification_channel_title))
            .setDescription(context.getString(R.string.tracelocation_notification_channel_description))
            .build()

        notificationManagerCompat.createNotificationChannel(channel)
    }

    fun newBaseBuilder(): NotificationCompat.Builder {
        val common = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_notification_icon_default_small)
            priority = NotificationCompat.PRIORITY_DEFAULT

            val pendingIntent = navDeepLinkBuilderFactory.create(context)
                .setGraph(R.navigation.trace_location_attendee_nav_graph)
                .setComponentName(MainActivity::class.java)
                .setDestination(R.id.checkInsFragment)
                .createPendingIntent()

            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        // Generic notification that does not exposure any specifics
        val public = common.apply {
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTitle(context.getString(R.string.notification_headline))
            setContentTextExpandable(context.getString(R.string.notification_body))
        }.build()

        return common.apply {
            setPublicVersion(public)
            setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        }
    }

    fun sendNotification(notificationId: Int, notification: Notification) {
        if (BuildVersionWrap.hasAPILevel(Build.VERSION_CODES.O) && !isNotificationChannelSetup) {
            isNotificationChannelSetup = true
            setupChannel()
        }
        Timber.i("Showing notification for ID=$notificationId: %s", notification)
        notificationManagerCompat.notify(notificationId, notification)
    }
}
