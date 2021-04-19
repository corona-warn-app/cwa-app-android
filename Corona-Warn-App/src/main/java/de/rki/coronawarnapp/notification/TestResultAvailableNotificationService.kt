package de.rki.coronawarnapp.notification

import android.content.Context
import androidx.annotation.IdRes
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Provider

abstract class TestResultAvailableNotificationService(
    private val context: Context,
    private val foregroundState: ForegroundState,
    private val navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>,
    private val notificationHelper: GeneralNotifications,
    private val cwaSettings: CWASettings,
    private val notificationId: NotificationId,
    @IdRes private val destination: Int
) {

    suspend fun showTestResultAvailableNotification(testResult: CoronaTestResult) {
        Timber.d("showTestResultAvailableNotification(testResult=%s)", testResult)

        if (foregroundState.isInForeground.first()) {
            Timber.d("App in foreground, skipping notification.")
            return
        }

        if (!cwaSettings.isNotificationsTestEnabled.value) {
            Timber.i("Don't show test result available notification because user doesn't want to be informed")
            return
        }

        val pendingIntent = navDeepLinkBuilderProvider.get().apply {
            setGraph(R.navigation.nav_graph)
            setComponentName(MainActivity::class.java)
            setDestination(getNotificationDestination(testResult))
        }.createPendingIntent()

        val notification = notificationHelper.newBaseBuilder().apply {
            setContentTitle(context.getString(R.string.notification_headline_test_result_ready))
            setContentTextExpandable(context.getString(R.string.notification_body_test_result_ready))
            setContentIntent(pendingIntent)
        }.build()

        Timber.i("Showing TestResultAvailable notification!")
        notificationHelper.sendNotification(
            notificationId = notificationId,
            notification = notification,
        )
    }

    fun cancelTestResultAvailableNotification() {
        notificationHelper.cancelCurrentNotification(notificationId)
    }

    /**
     * The pending result fragment will forward to the correct screen
     * Because we can't save the test result at the moment (legal),
     * it needs to be reloaded each time.
     * If we navigate directly to the positive/negative result screen,
     * then we also need to add explicit test loading logic there.
     * By letting the forwarding happen via the PendingResultFragment,
     * we have a common location to retrieve the test result.
     */
    fun getNotificationDestination(testResult: CoronaTestResult) = destination
}
