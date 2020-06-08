package de.rki.coronawarnapp.exception.reporting

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.exception.ExceptionCategory
import java.io.PrintWriter
import java.io.StringWriter

fun Throwable.report(exceptionCategory: ExceptionCategory) =
    this.report(exceptionCategory, null, null)

fun Throwable.report(
    exceptionCategory: ExceptionCategory,
    prefix: String?,
    suffix: String?
) {
    val intent = Intent(ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL)
    intent.putExtra(ReportingConstants.ERROR_REPORT_CATEGORY_EXTRA, exceptionCategory.name)
    intent.putExtra(ReportingConstants.ERROR_REPORT_PREFIX_EXTRA, prefix)
    intent.putExtra(ReportingConstants.ERROR_REPORT_SUFFIX_EXTRA, suffix)
    intent.putExtra(ReportingConstants.ERROR_REPORT_MESSAGE_EXTRA, this.message)
    val sw = StringWriter()
    this.printStackTrace()
    this.printStackTrace(PrintWriter(sw))
    intent.putExtra(ReportingConstants.ERROR_REPORT_STACK_EXTRA, sw.toString())
    LocalBroadcastManager.getInstance(CoronaWarnApplication.getAppContext()).sendBroadcast(intent)
}

fun reportGeneric(
    stackString: String
) {
    val intent = Intent(ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL)
    intent.putExtra("category", ExceptionCategory.INTERNAL.name)
    intent.putExtra("stack", stackString)
    LocalBroadcastManager.getInstance(CoronaWarnApplication.getAppContext()).sendBroadcast(intent)
}
