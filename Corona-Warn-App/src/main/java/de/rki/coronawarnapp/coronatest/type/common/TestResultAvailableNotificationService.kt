package de.rki.coronawarnapp.coronatest.type.common

import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationId
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingFragmentArgs
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Provider

open class TestResultAvailableNotificationService(
    private val context: Context,
    private val foregroundState: ForegroundState,
    private val navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>,
    private val notificationHelper: GeneralNotifications,
    private val notificationId: NotificationId,
    private val logTag: String,
) {

    suspend fun showTestResultAvailableNotification(test: BaseCoronaTest) {
        Timber.tag(logTag).v("showTestResultAvailableNotification(test=%s)", test)

        if (foregroundState.isInForeground.first()) {
            Timber.tag(logTag).d("App in foreground, skipping notification.")
            return
        }

        val pendingIntent = navDeepLinkBuilderProvider.get().apply {
            setGraph(R.navigation.nav_graph)
            setComponentName(MainActivity::class.java)
            setArguments(
                SubmissionTestResultPendingFragmentArgs(
                    testType = test.type,
                    testIdentifier = test.identifier
                ).toBundle()
            )
            /*
             * The pending result fragment will forward to the correct screen
             * Because we can't save the test result at the moment (legal),
             * it needs to be reloaded each time.
             * If we navigate directly to the positive/negative result screen,
             * then we also need to add explicit test loading logic there.
             * By letting the forwarding happen via the PendingResultFragment,
             * we have a common location to retrieve the test result.
             */
            setDestination(R.id.submissionTestResultPendingFragment)
        }.createPendingIntent()

        val notification = notificationHelper.newBaseBuilder().apply {
            setContentTitle(context.getString(R.string.notification_headline_test_result_ready))
            setContentTextExpandable(context.getString(R.string.notification_body_test_result_ready))
            setContentIntent(pendingIntent)
        }.build()

        Timber.tag(logTag).i("Showing test result notification($notificationId) for %s", test)
        notificationHelper.sendNotification(
            notificationId = notificationId,
            notification = notification,
        )
    }

    fun cancelTestResultAvailableNotification() {
        Timber.tag(logTag).i("Canceling test result notification($notificationId)")
        notificationHelper.cancelCurrentNotification(notificationId)
    }
}
