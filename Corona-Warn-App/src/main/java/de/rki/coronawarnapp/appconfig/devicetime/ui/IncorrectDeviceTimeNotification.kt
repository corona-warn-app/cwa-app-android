package de.rki.coronawarnapp.appconfig.devicetime.ui

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@Reusable
class IncorrectDeviceTimeNotification @Inject constructor(
    @AppContext private val context: Context,
    private val foregroundState: ForegroundState,
    private val navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>,
    private val notificationHelper: NotificationHelper
) {

    private val notificationId = NotificationConstants.INCORRECT_DEVICE_TIME_NOTIFICATION_ID

    suspend fun show(): Boolean {
        if (foregroundState.isInForeground.first()) {
            Timber.d("Not showing notification as app is in the foreground.")
            return false
        }

        val pendingIntent = navDeepLinkBuilderProvider.get().apply {
            setGraph(R.navigation.nav_graph)
            setComponentName(MainActivity::class.java)
            setDestination(R.id.mainFragment)
        }.createPendingIntent()

        val notification = notificationHelper.getBaseBuilder().apply {
            setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    context.getString(R.string.device_time_incorrect_dialog_body)
                )
            )

            setContentTitle(context.getString(R.string.device_time_incorrect_dialog_headline))
            setContentIntent(pendingIntent)
        }.build()

        notificationHelper.sendNotification(notificationId, notification)
        return true
    }

    fun dismiss() {
        notificationHelper.cancelCurrentNotification(notificationId)
    }
}
