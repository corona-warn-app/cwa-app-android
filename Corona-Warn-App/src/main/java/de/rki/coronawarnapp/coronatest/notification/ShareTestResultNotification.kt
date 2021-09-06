package de.rki.coronawarnapp.coronatest.notification

import android.content.Context
import android.os.Bundle
import androidx.navigation.NavDeepLinkBuilder
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_INITIAL_OFFSET
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_INTERVAL
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ShareTestResultNotification @Inject constructor(
    @AppContext private val context: Context,
    private val timeStamper: TimeStamper,
    private val notificationHelper: GeneralNotifications,
) {

    fun scheduleSharePositiveTestResultReminder(testType: CoronaTest.Type) {
        notificationHelper.scheduleRepeatingNotification(
            testType,
            timeStamper.nowUTC.plus(POSITIVE_RESULT_NOTIFICATION_INITIAL_OFFSET),
            POSITIVE_RESULT_NOTIFICATION_INTERVAL,
            POSITIVE_RESULT_NOTIFICATION_ID
        )
    }

    fun showSharePositiveTestResultNotification(notificationId: Int, testType: CoronaTest.Type) {
        Timber.tag(TAG).d("showSharePositiveTestResultNotification(notificationId=$notificationId)")

        val args = Bundle().apply { putSerializable("testType", testType) }

        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.nav_graph)
            .setComponentName(MainActivity::class.java)
            .setDestination(R.id.submissionTestResultAvailableFragment)
            .setArguments(args)
            .createPendingIntent()

        val notification = notificationHelper.newBaseBuilder().apply {
            setContentTitle(context.getString(R.string.notification_headline_share_positive_result))
            setContentTextExpandable(context.getString(R.string.notification_body_share_positive_result))
            setContentIntent(pendingIntent)
        }.build()

        notificationHelper.sendNotification(
            notificationId = notificationId,
            notification = notification,
        )
    }

    fun cancelSharePositiveTestResultNotification(testType: CoronaTest.Type) {
        notificationHelper.cancelFutureNotifications(POSITIVE_RESULT_NOTIFICATION_ID, testType)
        Timber.tag(TAG).v("Future positive test result notifications have been canceled")
    }

    companion object {
        private val TAG = tag<ShareTestResultNotification>()
    }
}
