package de.rki.coronawarnapp.eventregistration.common

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ApiLevel
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Helper to send notifications on the notification channel for trace location related events.
 *
 * Also see **[de.rki.coronawarnapp.notification.GeneralNotifications]**
 */
@Reusable
class TraceLocationNotifications @Inject constructor(
    @AppContext private val context: Context,
    private val apiLevel: ApiLevel,
    private val notificationManagerCompat: NotificationManagerCompat,
    private val notificationManager: NotificationManager,
) {

    private val channelId = "${context.packageName}.notification.traceLocationChannelId"

    @TargetApi(Build.VERSION_CODES.O)
    fun setupChannel() {
        Timber.d("setupChannel()")

        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.tracelocation_notification_channel_title),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.tracelocation_notification_channel_description)
        }

        notificationManager.createNotificationChannel(channel)
    }

    fun newBaseBuilder(): NotificationCompat.Builder = NotificationCompat.Builder(context, channelId).apply {
        setSmallIcon(R.drawable.ic_splash_logo)
        priority = NotificationCompat.PRIORITY_DEFAULT
        setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.trace_location_attendee_nav_graph)
            .setComponentName(MainActivity::class.java)
            .setDestination(R.id.checkInsFragment)
            .createPendingIntent()

        setContentIntent(pendingIntent)
        setAutoCancel(true)
    }

    fun sendNotification(notificationId: Int, notification: Notification) {
        if (apiLevel.hasAPILevel(Build.VERSION_CODES.O)) {
            setupChannel()
        }
        Timber.i("Showing notification for ID=$notificationId: %s", notification)
        notificationManagerCompat.notify(notificationId, notification)
    }
}
