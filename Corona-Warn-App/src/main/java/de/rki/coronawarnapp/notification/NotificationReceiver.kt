package de.rki.coronawarnapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.coronatest.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.notification.NotificationConstants.NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_LEGACY_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_PCR_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RAT_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_TEST_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_TEST_TYPE
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

typealias NotificationId = Int

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var shareTestResultNotificationService: ShareTestResultNotificationService

    @Suppress("MaxLineLength")
    override fun onReceive(context: Context, intent: Intent) {
        when (val notificationId = intent.getIntExtra(NOTIFICATION_ID, Int.MIN_VALUE)) {
            POSITIVE_LEGACY_RESULT_NOTIFICATION_ID,
            POSITIVE_PCR_RESULT_NOTIFICATION_ID,
            POSITIVE_RAT_RESULT_NOTIFICATION_ID -> {
                val testIdentifier = intent.getStringExtra(POSITIVE_RESULT_NOTIFICATION_TEST_ID)
                val testTypeRaw = intent.getStringExtra(POSITIVE_RESULT_NOTIFICATION_TEST_TYPE)
                val testType = BaseCoronaTest.Type.values().firstOrNull { it.raw == testTypeRaw }
                if (testType == null || testIdentifier == null) {
                    Timber.tag(TAG)
                        .e("Invalid arguments testTypeRaw = %s, testIdentifier = %s", testTypeRaw, testIdentifier)
                } else {
                    Timber.tag(TAG).v(
                        "NotificationReceiver received intent to show a positive test result notification for test type %s",
                        testType
                    )
                    shareTestResultNotificationService.maybeShowSharePositiveTestResultNotification(
                        notificationId,
                        testType,
                        testIdentifier
                    )
                }
            }
            else ->
                Timber.tag(TAG).d("NotificationReceiver received an undefined notificationId: %s", notificationId)
        }
    }

    companion object {
        private val TAG = tag<NotificationReceiver>()
    }
}
