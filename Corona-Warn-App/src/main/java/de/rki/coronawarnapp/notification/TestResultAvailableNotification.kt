package de.rki.coronawarnapp.notification

import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.formatter.TestResult
import javax.inject.Inject

class TestResultAvailableNotification @Inject constructor(
    @AppContext private val context: Context,
    private val timeStamper: TimeStamper
) {

    fun showTestResultNotification(notificationId: Int, testResult: TestResult) {
            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setComponentName(MainActivity::class.java)
                .setDestination(getNotificationDestination(testResult))
                .createPendingIntent()

            NotificationHelper.sendNotification(
                title = context.getString(R.string.notification_headline_share_positive_result),
                content = context.getString(R.string.notification_body_share_positive_result),
                notificationId = notificationId,
                pendingIntent = pendingIntent
            )
            NotificationHelper.cancelFutureNotifications(notificationId)
    }

    fun getNotificationDestination(testResult: TestResult): Int {
        return if(testResult == TestResult.POSITIVE) {
            R.id.submissionTestResultAvailableFragment
        } else {
            R.id.submissionResultFragment
        }
    }

}
