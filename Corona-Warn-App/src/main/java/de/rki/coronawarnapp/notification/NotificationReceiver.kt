package de.rki.coronawarnapp.notification


import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R

import de.rki.coronawarnapp.notification.NotificationConstants.NOTIFICATION_REQUEST_CODE_ID
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_REQUEST_CODE
import de.rki.coronawarnapp.ui.main.MainActivity
import timber.log.Timber


class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (val requestCode = intent.getIntExtra(NOTIFICATION_REQUEST_CODE_ID, Int.MIN_VALUE)) {
            POSITIVE_RESULT_NOTIFICATION_REQUEST_CODE ->
                showPositiveResultNotification(context, requestCode)
            else ->
                Timber.tag(TAG).v("NotificationReceiver received an undefined request code: %s", requestCode)
        }
    }

    private fun showPositiveResultNotification(context: Context, requestCode: Int) {
        val pendingIntent = PendingIntent.getActivity(
                CoronaWarnApplication.getAppContext(),
                requestCode,
                Intent(CoronaWarnApplication.getAppContext(), MainActivity::class.java),
                0
        )
        NotificationHelper.sendNotification(
            title = context.getString(R.string.notification_headline_share_positive_result),
            content = context.getString(R.string.notification_body_share_positive_result),
            visibility = NotificationCompat.VISIBILITY_PUBLIC,
            pendingIntent = pendingIntent
        )
    }

    companion object {
        private val TAG: String? = NotificationReceiver::class.simpleName
    }
}
