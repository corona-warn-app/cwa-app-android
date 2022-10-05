package de.rki.coronawarnapp.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_NO_CREATE
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.notification.NotificationConstants.NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_LEGACY_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_TEST_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_TEST_TYPE
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Helper for the apps main notification channel,
 * e.g. notifications about increased risk.
 * Notifications are IMPORTANCE_HIGH
 */
@Reusable
class GeneralNotifications @Inject constructor(
    @AppContext private val context: Context,
    private val notificationManagerCompat: NotificationManagerCompat
) {

    private var isNotificationChannelSetup = false

    fun setupNotificationChannel() {
        Timber.tag(TAG).d("setupChannel()")
        val channel = NotificationChannelCompat.Builder(
            MAIN_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(context.getString(R.string.general_notification_channel_title))
            .setDescription(context.getString(R.string.general_notification_channel_description))
            .setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

            ).build()
        notificationManagerCompat.createNotificationChannel(channel)
    }

    fun cancelFutureNotifications(notificationId: Int, testType: BaseCoronaTest.Type) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel legacy notifications at first
        val legacyPendingIntent =
            createPendingIntentToScheduleNotification(
                POSITIVE_LEGACY_RESULT_NOTIFICATION_ID,
                testType,
                null,
                FLAG_NO_CREATE
            )
        if (legacyPendingIntent != null) {
            manager.cancel(legacyPendingIntent)
            Timber.tag(TAG).v("Canceled future legacy notifications")
        } else {
            Timber.tag(TAG).v("No future legacy notifications")
        }

        val pendingIntent =
            createPendingIntentToScheduleNotification(notificationId, testType, null, FLAG_NO_CREATE)
        if (pendingIntent != null) {
            manager.cancel(pendingIntent)
            Timber.tag(TAG).v("Canceled future notifications with id:$notificationId type:$testType")
        } else {
            Timber.tag(TAG).v("No future notifications with id:$notificationId type:$testType")
        }
    }

    fun cancelCurrentNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
        Timber.tag(TAG).v("Canceled notifications with id: %s", notificationId)
    }

    fun scheduleRepeatingNotification(
        testType: BaseCoronaTest.Type,
        testIdentifier: TestIdentifier,
        initialTime: Instant,
        interval: Duration,
        notificationId: NotificationId
    ) {
        val pendingIntent = createPendingIntentToScheduleNotification(notificationId, testType, testIdentifier)
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setInexactRepeating(AlarmManager.RTC, initialTime.toEpochMilli(), interval.toMillis(), pendingIntent)
    }

    private fun createPendingIntentToScheduleNotification(
        notificationId: NotificationId,
        testType: BaseCoronaTest.Type,
        testIdentifier: TestIdentifier?,
        flag: Int = FLAG_CANCEL_CURRENT
    ) =
        PendingIntent.getBroadcast(
            context,
            notificationId,
            Intent(context, NotificationReceiver::class.java).apply {
                putExtra(NOTIFICATION_ID, notificationId)
                putExtra(POSITIVE_RESULT_NOTIFICATION_TEST_TYPE, testType.raw)
                putExtra(POSITIVE_RESULT_NOTIFICATION_TEST_ID, testIdentifier)
            },
            flag or FLAG_IMMUTABLE
        )

    fun newBaseBuilder(): NotificationCompat.Builder {
        val common = NotificationCompat.Builder(context, MAIN_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification_icon_default_small)
            priority = NotificationCompat.PRIORITY_MAX

            val defaultIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                FLAG_IMMUTABLE
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
        Timber.tag(TAG).i("Showing notification for ID=$notificationId: %s", notification)
        notificationManagerCompat.notify(notificationId, notification)
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal const val MAIN_CHANNEL_ID = "de.rki.coronawarnapp.notification.exposureNotificationChannelId"
        private const val TAG = "GeneralNotifications"
    }
}
