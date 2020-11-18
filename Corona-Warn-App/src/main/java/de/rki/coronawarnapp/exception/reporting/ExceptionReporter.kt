package de.rki.coronawarnapp.exception.reporting

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.ReportingConstants.STATUS_CODE_GOOGLE_API_FAIL
import de.rki.coronawarnapp.exception.reporting.ReportingConstants.STATUS_CODE_GOOGLE_UPDATE_NEEDED
import de.rki.coronawarnapp.exception.reporting.ReportingConstants.STATUS_CODE_REACHED_REQUEST_LIMIT
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.tryHumanReadableError
import java.io.PrintWriter
import java.io.StringWriter

fun Throwable.report(exceptionCategory: ExceptionCategory) {
    this.report(exceptionCategory, null, null)
}

fun Throwable.report(
    exceptionCategory: ExceptionCategory,
    prefix: String?,
    suffix: String?
) {
    reportProblem(tag = prefix, info = suffix)

    if (CWADebug.isAUnitTest) return

    val context = CoronaWarnApplication.getAppContext()

    val intent = Intent(ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL)
    intent.putExtra(ReportingConstants.ERROR_REPORT_CATEGORY_EXTRA, exceptionCategory.name)
    intent.putExtra(ReportingConstants.ERROR_REPORT_PREFIX_EXTRA, prefix)
    intent.putExtra(ReportingConstants.ERROR_REPORT_SUFFIX_EXTRA, suffix)

    if (this is HasHumanReadableError) {
        val humanReadable = this.tryHumanReadableError(context)
        humanReadable.title?.let {
            intent.putExtra(ReportingConstants.ERROR_REPORT_TITLE_EXTRA, it)
        }
        intent.putExtra(ReportingConstants.ERROR_REPORT_MESSAGE_EXTRA, humanReadable.description)
    } else {
        intent.putExtra(ReportingConstants.ERROR_REPORT_MESSAGE_EXTRA, this.message)
    }

    if (this is ReportedExceptionInterface) {
        intent.putExtra(ReportingConstants.ERROR_REPORT_CODE_EXTRA, this.code)
        this.resId?.let { intent.putExtra(ReportingConstants.ERROR_REPORT_RES_ID, it) }
    }

    var stackExtra = ""

    // override the message with a generic one if it is an ApiException
    if (this is ApiException) {

        var errorMessage = R.string.errors_communication_with_api

        if (this.statusCode == STATUS_CODE_GOOGLE_UPDATE_NEEDED) {
            errorMessage = R.string.errors_google_update_needed
        } else if (this.statusCode == STATUS_CODE_REACHED_REQUEST_LIMIT ||
            this.statusCode == STATUS_CODE_GOOGLE_API_FAIL
        ) {
            errorMessage = R.string.errors_google_api_error
        }

        intent.putExtra(ReportingConstants.ERROR_REPORT_RES_ID, errorMessage)
        intent.putExtra(ReportingConstants.ERROR_REPORT_CODE_EXTRA, ErrorCodes.API_EXCEPTION.code)
        intent.putExtra(ReportingConstants.ERROR_REPORT_API_EXCEPTION_CODE, this.statusCode)
    }

    if (stackExtra.isEmpty()) {
        val sw = StringWriter()
        this.printStackTrace()
        this.printStackTrace(PrintWriter(sw))
        stackExtra = sw.toString()
    }

    intent.putExtra(ReportingConstants.ERROR_REPORT_STACK_EXTRA, stackExtra)
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
}
