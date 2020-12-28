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
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationManagerCompat
import dagger.Reusable
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
    @AppContext private val context: Context,
    private val notificationManagerCompat: NotificationManagerCompat,
    private val notificationManager: NotificationManager
) {

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
     * @see audioAttributes
     * @see notificationManager
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channel = NotificationChannel(
                MAIN_CHANNEL_ID,
                context.getString(R.string.notification_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_description)
                setSound(notificationRingtone, audioAttributes)
            }

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
            flag
        )

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
        val builder = getBaseBuilder().apply {
            setContentIntent(pendingIntent)
            setVisibility(visibility)
        }

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

        return builder.build()
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
        sendNotification(notificationId, notification)
    }

    fun sendNotification(
        notificationId: NotificationId,
        notification: Notification
    ) {
        Timber.i("Showing notification for ID=$notificationId: %s", notification)
        notificationManagerCompat.notify(notificationId, notification)
    }

    fun getBaseBuilder() = NotificationCompat.Builder(context, MAIN_CHANNEL_ID).apply {
        setSmallIcon(R.drawable.ic_splash_logo)
        priority = NotificationCompat.PRIORITY_MAX
        setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        setContentIntent(createPendingIntentToMainActivity())
        setAutoCancel(true)
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal const val MAIN_CHANNEL_ID = "de.rki.coronawarnapp.notification.exposureNotificationChannelId"
    }
}
