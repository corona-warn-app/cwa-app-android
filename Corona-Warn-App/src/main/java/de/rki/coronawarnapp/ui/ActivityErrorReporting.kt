package de.rki.coronawarnapp.ui

import androidx.appcompat.app.AppCompatActivity
import de.rki.coronawarnapp.exception.handler.GlobalExceptionHandlerConstants
import de.rki.coronawarnapp.exception.reportGeneric

/**
 * If the app crashed in the last instance and was restarted, the stacktrace is retrieved
 * from the intent and displayed in a dialog report
 *
 * @see de.rki.coronawarnapp.exception.handler.GlobalExceptionHandler
 */
fun AppCompatActivity.showDialogWithStacktraceIfPreviouslyCrashed() {
    val appCrashedAndWasRestarted =
        intent.getBooleanExtra(GlobalExceptionHandlerConstants.APP_CRASHED, false)
    if (appCrashedAndWasRestarted) {
        val stackTrade = intent.getStringExtra(GlobalExceptionHandlerConstants.STACK_TRACE)
        if (!stackTrade.isNullOrEmpty()) {
            reportGeneric(stackTrade)
        }
    }
}
