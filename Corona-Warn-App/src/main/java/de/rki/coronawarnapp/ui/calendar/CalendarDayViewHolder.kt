package de.rki.coronawarnapp.ui.calendar

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import org.joda.time.LocalDate

/**
 * Calendar day view holder
 *
 * @param v View - view for holder
 */
class CalendarDayViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    /**
     * Day text view
     */
    private val textView: TextView = v.findViewById(R.id.dayText)

    /**
     * Bind data to view
     */
    fun bind(day:CalendarAdapter.Day, clickListener: (CalendarAdapter.Day) -> Unit) {
        val context = textView.context
        val today = LocalDate.now()

        // Set day text
        textView.text = day.date.dayOfMonth.toString()

        // If date is after today - then disable click listener
        if(!day.date.isAfter(today)) {
            textView.setOnClickListener { clickListener(day) }
        }

        // Update visuals
        when {
            // Selected
            day.isSelected -> {
                textView.setBackgroundResource(R.drawable.calendar_selected_day_back)
                textView.setTextColor(ContextCompat.getColor(context, R.color.colorTextEmphasizedButton))
            }
            // Today
            day.date.isEqual(today) -> {
                textView.setBackgroundResource(R.drawable.calendar_today_back)
                textView.setTextColor(ContextCompat.getColor(context, R.color.colorCalendarTodayText))
            }
            // Future
            day.date.isAfter(today) -> {
                textView.setBackgroundResource(0)
                textView.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary3))
            }
            // Past
            day.date.isBefore(today) -> {
                textView.setBackgroundResource(0)
                textView.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary1))
            }
        }
    }
}
