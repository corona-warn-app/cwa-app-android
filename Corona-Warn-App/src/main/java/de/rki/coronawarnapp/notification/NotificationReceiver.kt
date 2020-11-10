package de.rki.coronawarnapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import de.rki.coronawarnapp.notification.NotificationConstants.NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_ID
import timber.log.Timber
import javax.inject.Inject

typealias NotificationId = Int

class NotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var testResultNotificationService: TestResultNotificationService

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        when (val notificationId = intent.getIntExtra(NOTIFICATION_ID, Int.MIN_VALUE)) {
            POSITIVE_RESULT_NOTIFICATION_ID -> {
                Timber.tag(TAG).v("NotificationReceiver received intent to show a positive test result notification")
                testResultNotificationService.showPositiveTestResultNotification(notificationId)
            }
            else ->
                Timber.tag(TAG).d("NotificationReceiver received an undefined notificationId: %s", notificationId)
        }
    }

    companion object {
        private val TAG: String? = NotificationReceiver::class.simpleName
    }
}
