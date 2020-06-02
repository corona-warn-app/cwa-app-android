package de.rki.coronawarnapp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.ui.main.MainActivity
import kotlin.random.Random

/**
 * Singleton class for notification handling
 * The helper uses externalised constants for readability.
 *
 * @see NotificationConstants
 */
object NotificationHelper {

    private val TAG: String? = NotificationHelper::class.simpleName

    /**
     * Notification channel id
     *
     * @see NotificationConstants.NOTIFICATION_CHANNEL_ID
     */
    private val channelId =
        CoronaWarnApplication.getAppContext().getString(NotificationConstants.NOTIFICATION_CHANNEL_ID)

    /**
     * Notification manager
     */
    private val notificationManager =
        CoronaWarnApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            val channelName = CoronaWarnApplication.getAppContext().getString(NotificationConstants.CHANNEL_NAME)

            val notificationRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)

            channel.description =
                CoronaWarnApplication.getAppContext().getString(NotificationConstants.CHANNEL_DESCRIPTION)
            channel.setSound(notificationRingtone, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }
    }

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
    private fun buildNotification(title: String, content: String, visibility: Int): Notification? {
        val builder = NotificationCompat.Builder(CoronaWarnApplication.getAppContext(), channelId)
            .setSmallIcon(NotificationConstants.NOTIFICATION_SMALL_ICON)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(visibility)
            .setContentIntent(createPendingIntentToMainActivity())
            .setAutoCancel(true)

        if (visibility == NotificationCompat.VISIBILITY_PRIVATE) {
            builder.setPublicVersion(buildNotification(title, content, NotificationCompat.VISIBILITY_PUBLIC))
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
            CoronaWarnApplication.getAppContext(),
            0,
            Intent(CoronaWarnApplication.getAppContext(), MainActivity::class.java),
            0
        )

    /**
     * Send notification
     * Build and send notification with predefined title, content and visibility.
     *
     * @param title: String
     * @param content: String
     * @param visibility: Int
     */
    fun sendNotification(title: String, content: String, visibility: Int) {
        val notification = buildNotification(title, content, visibility) ?: return
        with(NotificationManagerCompat.from(CoronaWarnApplication.getAppContext())) {
            notify(Random.nextInt(), notification)
        }
    }

    /**
     * Send notification
     * Build and send notification with predefined title and content.
     * Visibility is auto set to NotificationCompat.VISIBILITY_PRIVATE
     *
     * @param title: String
     * @param content: String
     *
     * @see NotificationCompat.VISIBILITY_PRIVATE
     */
    fun sendNotification(title: String, content: String) {
        sendNotification(title, content, NotificationCompat.VISIBILITY_PRIVATE)
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
                Log.d(TAG, "Notification build successfully.")
            } else {
                Log.d(TAG, "Notification build failed.")
            }
        }
    }
}
