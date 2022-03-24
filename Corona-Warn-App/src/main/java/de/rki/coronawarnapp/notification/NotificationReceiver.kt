package de.rki.coronawarnapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import de.rki.coronawarnapp.coronatest.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.notification.NotificationConstants.NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_LEGACY_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_PCR_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RAT_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_TEST_TYPE
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

typealias NotificationId = Int

class NotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var shareTestResultNotificationService: ShareTestResultNotificationService

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        when (val notificationId = intent.getIntExtra(NOTIFICATION_ID, Int.MIN_VALUE)) {
            POSITIVE_LEGACY_RESULT_NOTIFICATION_ID,
            POSITIVE_PCR_RESULT_NOTIFICATION_ID,
            POSITIVE_RAT_RESULT_NOTIFICATION_ID -> {
                val testTypeRaw = intent.getStringExtra(POSITIVE_RESULT_NOTIFICATION_TEST_TYPE)
                val testType = BaseCoronaTest.Type.values().first { it.raw == testTypeRaw }
                Timber.tag(TAG).v(
                    "NotificationReceiver received intent to show a positive test result notification for test type %s",
                    testType
                )
                shareTestResultNotificationService.maybeShowSharePositiveTestResultNotification(
                    notificationId,
                    testType
                )
            }
            else ->
                Timber.tag(TAG).d("NotificationReceiver received an undefined notificationId: %s", notificationId)
        }
    }

    companion object {
        private val TAG = tag<NotificationReceiver>()
    }
}
