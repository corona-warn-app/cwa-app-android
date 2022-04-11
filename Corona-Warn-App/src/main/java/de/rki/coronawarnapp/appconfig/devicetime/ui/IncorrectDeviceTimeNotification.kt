package de.rki.coronawarnapp.appconfig.devicetime.ui

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class IncorrectDeviceTimeNotification @Inject constructor(
    @AppContext private val context: Context,
    private val foregroundState: ForegroundState,
    private val navDeepLinkBuilderFactory: NavDeepLinkBuilderFactory,
    private val notificationHelper: GeneralNotifications
) {

    private val notificationId = NotificationConstants.INCORRECT_DEVICE_TIME_NOTIFICATION_ID

    suspend fun show(): Boolean {
        if (foregroundState.isInForeground.first()) {
            Timber.d("Not showing notification as app is in the foreground.")
            return false
        }

        val pendingIntent = navDeepLinkBuilderFactory.create(context).apply {
            setGraph(R.navigation.nav_graph)
            setComponentName(MainActivity::class.java)
            setDestination(R.id.mainFragment)
        }.createPendingIntent()

        val notification = notificationHelper.newBaseBuilder().apply {
            setContentIntent(pendingIntent)
            setContentTitle(context.getString(R.string.device_time_incorrect_dialog_headline))
            setContentTextExpandable(context.getString(R.string.device_time_incorrect_dialog_body))
        }.build()

        notificationHelper.sendNotification(notificationId, notification)
        return true
    }

    fun dismiss() {
        notificationHelper.cancelCurrentNotification(notificationId)
    }
}
