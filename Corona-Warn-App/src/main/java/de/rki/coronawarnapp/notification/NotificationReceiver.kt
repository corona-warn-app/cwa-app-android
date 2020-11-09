package de.rki.coronawarnapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.rki.coronawarnapp.notification.NotificationConstants.NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.util.coroutine.AppScope
import timber.log.Timber
import javax.inject.Inject

typealias NotificationId = Int

class NotificationReceiver : BroadcastReceiver() {

    @Inject @AppScope lateinit var testResultNotificationService: TestResultNotificationService

    override fun onReceive(context: Context, intent: Intent) {
        when (val notificationId = intent.getIntExtra(NOTIFICATION_ID, Int.MIN_VALUE)) {
            POSITIVE_RESULT_NOTIFICATION_ID ->
                testResultNotificationService.showPositiveTestResultNotification(notificationId)
            else ->
                Timber.tag(TAG).v("NotificationReceiver received an undefined notificationId: %s", notificationId)
        }
    }

    companion object {
        private val TAG: String? = NotificationReceiver::class.simpleName
    }
}
