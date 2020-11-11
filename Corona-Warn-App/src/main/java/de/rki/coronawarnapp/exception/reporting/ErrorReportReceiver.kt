package de.rki.coronawarnapp.exception.reporting

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.util.DialogHelper
import timber.log.Timber
import java.util.Locale

class ErrorReportReceiver(private val activity: Activity) : BroadcastReceiver() {

    @Suppress("LongMethod")
    override fun onReceive(context: Context, intent: Intent) {
        val category = ExceptionCategory
            .valueOf(intent.getStringExtra(ReportingConstants.ERROR_REPORT_CATEGORY_EXTRA) ?: "")

        val prefix = intent.getStringExtra(ReportingConstants.ERROR_REPORT_PREFIX_EXTRA)
        val suffix = intent.getStringExtra(ReportingConstants.ERROR_REPORT_SUFFIX_EXTRA)

        // set the message of the dialog: default is technical
        var message = intent.getStringExtra(ReportingConstants.ERROR_REPORT_MESSAGE_EXTRA)
            ?: context.resources.getString(R.string.errors_generic_text_unknown_error_cause)

        // if we have a res id we set that message
        if (intent.hasExtra(ReportingConstants.ERROR_REPORT_RES_ID)) {
            val resId = intent.getIntExtra(ReportingConstants.ERROR_REPORT_RES_ID, 0)
            message = context.resources.getString(resId)
        }

        val stack = intent.getStringExtra(ReportingConstants.ERROR_REPORT_STACK_EXTRA)
        val title = context.resources.getString(R.string.errors_generic_headline)
        val confirm = context.resources.getString(R.string.errors_generic_button_positive)
        val details = context.resources.getString(R.string.errors_generic_button_negative)

        val detailsTitle = context.resources.getString(R.string.errors_generic_details_headline)

        if (intent.hasExtra(ReportingConstants.ERROR_REPORT_API_EXCEPTION_CODE)) {
            val apiStatusCode = intent.getIntExtra(
                ReportingConstants.ERROR_REPORT_API_EXCEPTION_CODE,
                ErrorCodes.REPORTED_EXCEPTION_UNKNOWN_PROBLEM.code
            )

            message += "#$apiStatusCode"
        }

        val dialogTitle = if (intent.getStringExtra(ReportingConstants.ERROR_REPORT_TITLE_EXTRA) != null) {
            intent.getStringExtra(ReportingConstants.ERROR_REPORT_TITLE_EXTRA)
        } else {
            val errorTitle = context.resources.getString(R.string.errors_generic_details_headline)
                .toUpperCase(Locale.ROOT)
            val errorCode = intent.getIntExtra(
                ReportingConstants.ERROR_REPORT_CODE_EXTRA,
                ReportingConstants.ERROR_REPORT_UNKNOWN_ERROR
            )
            "$errorTitle: $errorCode\n$title"
        }

        Timber.e("[$category]${(prefix ?: "")} $message${(suffix ?: "")}")

        if (!CoronaWarnApplication.isAppInForeground) {
            Timber.v("Not displaying error dialog, not in foreground.")
            return
        }

        val dialogInstance = DialogHelper.DialogInstance(
            context = activity,
            title = dialogTitle,
            message = message,
            positiveButton = confirm,
            negativeButton = details,
            cancelable = null,
            positiveButtonFunction = {},
            negativeButtonFunction = {
                val stackTraceDialog = DialogHelper.DialogInstance(
                    activity,
                    title,
                    "$detailsTitle:\n$stack",
                    confirm
                )
                DialogHelper.showDialog(stackTraceDialog.copy(isTextSelectable = true))
                Unit
            }
        )
        DialogHelper.showDialog(dialogInstance)
    }
}
