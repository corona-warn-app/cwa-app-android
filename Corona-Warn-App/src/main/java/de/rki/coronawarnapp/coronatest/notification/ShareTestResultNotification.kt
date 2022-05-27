package de.rki.coronawarnapp.coronatest.notification

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.submission.warnothers.SubmissionResultPositiveOtherWarningNoConsentFragmentArgs
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ShareTestResultNotification @Inject constructor(
    @AppContext private val context: Context,
    private val timeStamper: TimeStamper,
    private val notificationHelper: GeneralNotifications,
    private val navDeepLinkBuilderFactory: NavDeepLinkBuilderFactory,
) {

    fun scheduleSharePositiveTestResultReminder(
        testType: BaseCoronaTest.Type,
        testIdentifier: TestIdentifier,
        notificationId: Int
    ) {
        notificationHelper.scheduleRepeatingNotification(
            testType,
            testIdentifier,
            timeStamper.nowJavaUTC.plus(NotificationConstants.POSITIVE_RESULT_NOTIFICATION_INITIAL_OFFSET),
            NotificationConstants.POSITIVE_RESULT_NOTIFICATION_INTERVAL,
            notificationId,
        )
    }

    fun showSharePositiveTestResultNotification(notificationId: Int, testIdentifier: TestIdentifier) {
        Timber.tag(TAG).d("showSharePositiveTestResultNotification(notificationId=$notificationId)")

        val args = SubmissionResultPositiveOtherWarningNoConsentFragmentArgs(testIdentifier = testIdentifier)
            .toBundle()

        val pendingIntent = navDeepLinkBuilderFactory.create(context)
            .setGraph(R.navigation.nav_graph)
            .setComponentName(MainActivity::class.java)
            .setDestination(R.id.submissionResultPositiveOtherWarningNoConsentFragment)
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

    fun cancelSharePositiveTestResultNotification(testType: BaseCoronaTest.Type, notificationId: Int) {
        notificationHelper.cancelFutureNotifications(notificationId, testType)
        Timber.tag(TAG).v("Future positive test result notifications have been canceled")
    }

    companion object {
        private val TAG = tag<ShareTestResultNotification>()
    }
}
