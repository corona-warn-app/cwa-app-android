package de.rki.coronawarnapp.notification

import android.annotation.TargetApi
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
import androidx.core.app.NotificationManagerCompat
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.notification.NotificationConstants.NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_TEST_TYPE
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

/**
 * Helper for the apps main notification channel,
 * e.g. notifications about increased risk.
 * Notifications are IMPORTANCE_HIGH
 */
@Reusable
class GeneralNotifications @Inject constructor(
    @AppContext private val context: Context,
    private val notificationManagerCompat: NotificationManagerCompat,
    private val notificationManager: NotificationManager
) {

    private var isNotificationChannelSetup = false

    @TargetApi(Build.VERSION_CODES.O)
    private fun setupNotificationChannel() {
        Timber.d("setupChannel()")

        val channel = NotificationChannel(
            MAIN_CHANNEL_ID,
            context.getString(R.string.general_notification_channel_title),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.general_notification_channel_description)

            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder().apply {
                    setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                }.build()
            )
        }

        notificationManager.createNotificationChannel(channel)
    }

    fun cancelFutureNotifications(notificationId: Int, testType: CoronaTest.Type) {
        val pendingIntent = createPendingIntentToScheduleNotification(notificationId, testType)
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
        testType: CoronaTest.Type,
        initialTime: Instant,
        interval: Duration,
        notificationId: NotificationId
    ) {
        val pendingIntent = createPendingIntentToScheduleNotification(notificationId, testType)
        val manager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setInexactRepeating(AlarmManager.RTC, initialTime.millis, interval.millis, pendingIntent)
    }

    private fun createPendingIntentToScheduleNotification(
        notificationId: NotificationId,
        testType: CoronaTest.Type,
        flag: Int = FLAG_CANCEL_CURRENT
    ) =
        PendingIntent.getBroadcast(
            context,
            notificationId,
            Intent(context, NotificationReceiver::class.java).apply {
                putExtra(NOTIFICATION_ID, notificationId)
                putExtra(POSITIVE_RESULT_NOTIFICATION_TEST_TYPE, testType.raw)
            },
            flag
        )

    fun newBaseBuilder(): NotificationCompat.Builder {
        val common = NotificationCompat.Builder(context, MAIN_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification_icon_default_small)
            priority = NotificationCompat.PRIORITY_MAX

            val defaultIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                0
            )
            setContentIntent(defaultIntent)
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

    fun sendNotification(notificationId: NotificationId, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isNotificationChannelSetup) {
            isNotificationChannelSetup = true
            setupNotificationChannel()
        }
        Timber.i("Showing notification for ID=$notificationId: %s", notification)
        notificationManagerCompat.notify(notificationId, notification)
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal const val MAIN_CHANNEL_ID = "de.rki.coronawarnapp.notification.exposureNotificationChannelId"
    }
}
