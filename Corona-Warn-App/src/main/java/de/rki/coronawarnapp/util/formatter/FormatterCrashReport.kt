@file:JvmName("FormatterCrashReport")

package de.rki.coronawarnapp.util.formatter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.rki.coronawarnapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@BindingAdapter("crashReportId")
fun formatCrashReportId(textView: TextView, id: Long) {
    textView.text = textView.context.getString(R.string.crash_report_title, id)
}

@BindingAdapter("crashReportDate")
fun formatCrashReportDate(textView: TextView, date: Date) {
    val sdf = SimpleDateFormat("HH:mm, E, dd.MM.yyyy", Locale.getDefault())
    textView.text = sdf.format(date)
}
