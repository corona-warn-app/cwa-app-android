package de.rki.coronawarnapp.exception

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.DialogHelper

class ErrorReportReceiver(private val activity: Activity) : BroadcastReceiver() {
    companion object {
        private val TAG: String = ErrorReportReceiver::class.java.simpleName
    }
    override fun onReceive(context: Context, intent: Intent) {
        val category = ExceptionCategory
            .valueOf(intent.getStringExtra(ReportingConstants.ERROR_REPORT_CATEGORY_EXTRA) ?: "")
        val prefix = intent.getStringExtra(ReportingConstants.ERROR_REPORT_PREFIX_EXTRA)
        val suffix = intent.getStringExtra(ReportingConstants.ERROR_REPORT_SUFFIX_EXTRA)
        val message = intent.getStringExtra(ReportingConstants.ERROR_REPORT_MESSAGE_EXTRA)
            ?: context.resources.getString(R.string.errors_generic_text_unknown_error_cause)
        val stack = intent.getStringExtra(ReportingConstants.ERROR_REPORT_STACK_EXTRA)
        val title = context.resources.getString(R.string.errors_generic_headline)
        val confirm = context.resources.getString(R.string.errors_generic_button_positive)
        val details = context.resources.getString(R.string.errors_generic_button_negative)
        val detailsTitle = context.resources.getString(R.string.errors_generic_details_headline)
        if (CoronaWarnApplication.isAppInForeground) {
            DialogHelper.showDialog(DialogHelper.DialogInstance(
                activity,
                title,
                message,
                confirm,
                details,
                null,
                {},
                {
                    DialogHelper.showDialog(
                        DialogHelper.DialogInstance(
                            activity,
                            title,
                            "$detailsTitle:\n$stack",
                            confirm
                        )).run {}
                }
            ))
        }
        Log.e(
            TAG,
            "[$category]${(prefix ?: "")} $message${(suffix ?: "")}"
        )
    }
}
