package de.rki.coronawarnapp.deadman

import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DeadmanNotificationSender @Inject constructor(
    @AppContext private val context: Context,
    private val foregroundState: ForegroundState,
    private val notificationHelper: NotificationHelper
) {

    suspend fun sendNotification() {
        if (foregroundState.isInForeground.first()) {
            Timber.tag(TAG).d("Not sending notification as we are in the foreground.")
            return
        }

        val notification = notificationHelper.getBaseBuilder().apply {
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTitle(context.getString(R.string.risk_details_deadman_notification_title))

            val content = context.getString(R.string.risk_details_deadman_notification_body)
            setContentText(content)
            setStyle(NotificationCompat.BigTextStyle().bigText(content))
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
