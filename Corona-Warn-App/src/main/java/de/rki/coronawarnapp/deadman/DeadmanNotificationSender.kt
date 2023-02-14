package de.rki.coronawarnapp.deadman

import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DeadmanNotificationSender @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foregroundState: ForegroundState,
    private val notificationHelper: GeneralNotifications
) {

    suspend fun sendNotification() {
        if (foregroundState.isInForeground.first()) {
            Timber.tag(TAG).d("Not sending notification as we are in the foreground.")
            return
        }

        val notification = notificationHelper.newBaseBuilder().apply {
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTitle(context.getString(R.string.risk_details_deadman_notification_title))
            setContentTextExpandable(
                context.getString(R.string.risk_details_deadman_notification_body)
            )
        }.build()

        notificationHelper.sendNotification(
            NotificationConstants.DEADMAN_NOTIFICATION_ID,
            notification
        )
    }

    companion object {
        private const val TAG = "DeadmanNotification"
    }
}
