package de.rki.coronawarnapp.exception

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.util.DialogHelper

class ErrorReportReceiver(private val activity: Activity) : BroadcastReceiver() {
    companion object {
        private val TAG: String = ErrorReportReceiver::class.java.simpleName
    }
    override fun onReceive(context: Context, intent: Intent) {
        val category = ExceptionCategory.valueOf(intent.getStringExtra("category") ?: "")
        val prefix = intent.getStringExtra("prefix")
        val suffix = intent.getStringExtra("suffix")
        val message = intent.getStringExtra("message")
        val title = context.resources.getString(R.string.errors_storage_headline)
        val confirm = context.resources.getString(R.string.errors_storage_button_positive)
        if (CoronaWarnApplication.isAppInForeground) {
            DialogHelper.showDialog(DialogHelper.DialogInstance(
                activity,
                title,
                message,
                confirm
            ))
        } else {
            NotificationHelper.sendNotification(
                title,
                message ?: "",
                NotificationCompat.PRIORITY_HIGH
            )
        }
        Log.e(
            TAG,
            "[$category]${(prefix ?: "")} ${(message ?: "Error Text Unavailable")}${(suffix ?: "")}"
        )
    }
}
