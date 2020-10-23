@file:JvmName("FormatterCrashReport")

package de.rki.coronawarnapp.util.formatter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.rki.coronawarnapp.R
import org.joda.time.DateTimeZone
import org.joda.time.Instant

@BindingAdapter("crashReportTitle")
fun formatCrashReportId(textView: TextView, id: Int) {
    textView.text = textView.context.getString(R.string.crash_report_title, id)
}

@BindingAdapter("crashReportDate")
fun formatCrashReportDate(textView: TextView, dateInstant: Instant) {
    val output: String =
        dateInstant.toDateTime(DateTimeZone.getDefault()).toString().replace("T", "  ")
    textView.text = output
}

@BindingAdapter("crashReportShortMessage")
fun formatCrashReportDate(textView: TextView, message: String) {
    textView.text = message
}
