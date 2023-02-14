package de.rki.coronawarnapp.presencetracing.checkins.checkout

import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.presencetracing.common.PresenceTracingNotifications
import de.rki.coronawarnapp.ui.launcher.LauncherActivity
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CheckOutNotification @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foregroundState: ForegroundState,
    private val notificationHelper: PresenceTracingNotifications,
    private val deepLinkBuilderFactory: NavDeepLinkBuilderFactory,
) {

    suspend fun showAutoCheckoutNotification(checkInId: Long) {
        Timber.d("showAutoCheckoutNotification(checkInId=$checkInId)")

        if (foregroundState.isInForeground.first()) {
            Timber.d("Not sending notification as we are in the foreground.")
            return
        }

        val pendingIntent = deepLinkBuilderFactory.create(context)
            .setGraph(R.navigation.nav_graph)
            .setComponentName(LauncherActivity::class.java)
            .setDestination(R.id.checkInsFragment)
            .createPendingIntent()

        val notification = notificationHelper.newBaseBuilder().apply {
            setContentIntent(pendingIntent)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTitle(context.getString(R.string.tracelocation_notification_autocheckout_title))
            setContentTextExpandable(context.getString(R.string.tracelocation_notification_autocheckout_description))
        }.build()

        notificationHelper.sendNotification(
            NotificationConstants.TRACELOCATION_AUTOCHECKOUT_NOTIFICATION_ID,
            notification
        )
    }
}
