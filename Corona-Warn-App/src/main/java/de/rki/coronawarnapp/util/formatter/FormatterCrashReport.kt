@file:JvmName("FormatterCrashReport")

package de.rki.coronawarnapp.util.formatter

import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import de.rki.coronawarnapp.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

@BindingAdapter("crashReportTitle")
fun formatCrashReportId(textView: TextView, id: Int) {
    textView.text = textView.context.getString(R.string.crash_report_title, id)
}

@RequiresApi(Build.VERSION_CODES.O)
@BindingAdapter("crashReportDate")
fun formatCrashReportDate(textView: TextView, dateInstant: Instant) {
    val myDate = Date.from(dateInstant)
    val sdf = SimpleDateFormat("Edd.MM.yyyy, HH:mm:ss", Locale.getDefault())
    textView.text = sdf.format(myDate)
}

@BindingAdapter("crashReportShortMessage")
fun formatCrashReportDate(textView: TextView, message: String) {
    textView.text = message
}
