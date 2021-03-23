package de.rki.coronawarnapp.eventregistration.checkins.checkout

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.eventregistration.common.TraceLocationNotifications
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class AutoCheckoutNotification @Inject constructor(
    @AppContext private val context: Context,
    private val foregroundState: ForegroundState,
    private val notificationHelper: TraceLocationNotifications,
) {

    suspend fun showAutoCheckoutNotification(checkInId: Long) {
        Timber.d("showAutoCheckoutNotification(checkInId=$checkInId)")

        if (foregroundState.isInForeground.first()) {
            Timber.d("Not sending notification as we are in the foreground.")
            return
        }

        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.trace_location_attendee_nav_graph)
            .setComponentName(MainActivity::class.java)
            .setDestination(R.id.checkInsFragment)
            .createPendingIntent()

        val notification = notificationHelper.newBaseBuilder().apply {
            setContentTitle(context.getString(R.string.tracelocation_notification_autocheckout_title))
            val content = context.getString(R.string.tracelocation_notification_autocheckout_description)
            setContentText(content)
            setStyle(NotificationCompat.BigTextStyle().bigText(content))
            setContentIntent(pendingIntent)
        }.build()

        notificationHelper.sendNotification(
            NotificationConstants.TRACELOCATION_AUTOCHECKOUT_NOTIFICATION_ID,
            notification
        )
    }
}
