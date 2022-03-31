package de.rki.coronawarnapp.familytest.core.notification

import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants.FAMILY_TEST_RESULT_AVAILABLE_NOTIFICATION_ID
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class FamilyTestNotificationService @Inject constructor(
    @AppContext private val context: Context,
    private val navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>,
    private val notificationHelper: GeneralNotifications,
) {

    fun showTestResultNotification() {
        val pendingIntent = navDeepLinkBuilderProvider.get().apply {
            setGraph(R.navigation.nav_graph)
            setComponentName(MainActivity::class.java)
            setDestination(R.id.familyTestListFragment)
        }.createPendingIntent()

        val notification = notificationHelper.newBaseBuilder().apply {
            setContentTitle(context.getString(R.string.notification_headline_test_result_ready))
            setContentTextExpandable(context.getString(R.string.notification_body_test_result_ready))
            setContentIntent(pendingIntent)
        }.build()

        Timber.tag(TAG).i("Family test result notification($FAMILY_TEST_RESULT_AVAILABLE_NOTIFICATION_ID)")
        notificationHelper.sendNotification(
            notificationId = FAMILY_TEST_RESULT_AVAILABLE_NOTIFICATION_ID,
            notification = notification,
        )
    }

    companion object {
        private val TAG = tag<FamilyTestNotificationService>()
    }
}
