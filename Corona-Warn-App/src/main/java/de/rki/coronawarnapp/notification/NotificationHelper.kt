package de.rki.coronawarnapp.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationManagerCompat
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationConstants.NOTIFICATION_ID
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.di.AppContext
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

/**
 * Singleton class for notification handling
 * Notifications should only be sent when the app is not in foreground.
 * The helper uses externalised constants for readability.
 *
 * @see NotificationConstants
 */
@Reusable
class NotificationHelper @Inject constructor(
    @AppContext private val context: Context
) {

    /**
     * Notification channel id
     *
     * @see NotificationConstants.NOTIFICATION_CHANNEL_ID
     */
    private val channelId = context.getString(NotificationConstants.NOTIFICATION_CHANNEL_ID)

    /**
     * Notification manager
     */
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Notification channel audio attributes
     */
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    /**
     * Create notification channel
     * Notification channel is only needed for API version >= 26.
     * Safe to be called repeatedly.
     *
     * @see NotificationConstants.CHANNEL_NAME
     * @see NotificationConstants.CHANNEL_DESCRIPTION
     * @see audioAttributes
     * @see notificationManager
     * @see channelId
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName =
                context.getString(NotificationConstants.CHANNEL_NAME)

            val notificationRingtone =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)

            channel.description =
                context
                    .getString(NotificationConstants.CHANNEL_DESCRIPTION)
            channel.setSound(notificationRingtone, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun cancelFutureNotifications(notificationId: Int) {
        val pendingIntent = createPendingIntentToScheduleNotification(notificationId)
        val manager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pendingIntent)
        Timber.v("Canceled future notifications with id: %s", notificationId)
    }

    fun cancelCurrentNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
        Timber.v("Canceled notifications with id: %s", notificationId)
    }

    fun scheduleRepeatingNotification(
        initialTime: Instant,
        interval: Duration,
        notificationId: NotificationId
    ) {
        val pendingIntent = createPendingIntentToScheduleNotification(notificationId)
        val manager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setInexactRepeating(AlarmManager.RTC, initialTime.millis, interval.millis, pendingIntent)
    }

    private fun createPendingIntentToScheduleNotification(
        notificationId: NotificationId,
        flag: Int = FLAG_CANCEL_CURRENT
    ) =
        PendingIntent.getBroadcast(
            context,
            notificationId,
            Intent(context, NotificationReceiver::class.java).apply {
                putExtra(NOTIFICATION_ID, notificationId)
            },
            flag)

    /**
     * Build notification
     * Create notification with defined title, content text and visibility.
     *
     * @param title: String
     * @param content: String
     * @param visibility: Int
     *
     * @return Notification?
     *
     * @see NotificationCompat.VISIBILITY_PUBLIC
     */
    private fun buildNotification(
        title: String,
        content: String,
        visibility: Int,
        expandableLongText: Boolean = false,
        pendingIntent: PendingIntent = createPendingIntentToMainActivity()
    ): Notification? {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(NotificationConstants.NOTIFICATION_SMALL_ICON)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(visibility)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (expandableLongText) {
            builder
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(content)
                )
        }

        if (title.isNotEmpty()) {
            builder.setContentTitle(title)
        }

        if (visibility == NotificationCompat.VISIBILITY_PRIVATE) {
            builder.setPublicVersion(
                buildNotification(
                    title,
                    content,
                    NotificationCompat.VISIBILITY_PUBLIC
                )
            )
        } else if (visibility == NotificationCompat.VISIBILITY_PUBLIC) {
            builder.setContentText(content)
        }

        return builder.build().also { logNotificationBuild(it) }
    }

    /**
     * Create pending intent to main activity
     *
     * @return PendingIntent
     */
    private fun createPendingIntentToMainActivity() =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            0
        )

    /**
     * Send notification
     * Build and send notification with predefined title, content and visibility.
     *
     * @param title: String
     * @param content: String
     * @param expandableLongText: Boolean
     * @param notificationId: NotificationId
     * @param pendingIntent: PendingIntent
     */
    fun sendNotification(
        title: String = context.getString(R.string.notification_name),
        content: String,
        notificationId: NotificationId,
        expandableLongText: Boolean = false,
        pendingIntent: PendingIntent = createPendingIntentToMainActivity()
    ) {
        Timber.d("Sending notification with id: %s | title: %s | content: %s", notificationId, title, content)
        val notification =
            buildNotification(title, content, PRIORITY_HIGH, expandableLongText, pendingIntent) ?: return
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notification)
        }
    }

    /**
     * Log notification build
     * Log success or failure of creating new notification
     *
     * @param notification: Notification?
     */
    private fun logNotificationBuild(notification: Notification?) {
        if (BuildConfig.DEBUG) {
            if (notification != null) {
                Timber.d("Notification build successfully.")
            } else {
                Timber.d("Notification build failed.")
            }
        }
    }
}
