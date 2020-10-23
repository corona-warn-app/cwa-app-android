@file:JvmName("FormatterCrashReport")

package de.rki.coronawarnapp.util.formatter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.rki.coronawarnapp.R
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@BindingAdapter("crashReportTitle")
fun formatCrashReportId(textView: TextView, id: Int) {
    textView.text = textView.context.getString(R.string.crash_report_title, id)
}


@BindingAdapter("crashReportDate")
fun formatCrashReportDate(textView: TextView, dateInstant: Instant) {
    val zone = DateTimeZone.getDefault()
    val mydate: LocalDate = dateInstant.toDateTime(zone).toLocalDate()
    //val myDate = Date.from(dateInstant)
    val sdf = SimpleDateFormat("Edd.MM.yyyy, HH:mm:ss", Locale.getDefault())
    textView.text = sdf.format(mydate)
}

@BindingAdapter("crashReportShortMessage")
fun formatCrashReportDate(textView: TextView, message: String) {
    textView.text = message
}
