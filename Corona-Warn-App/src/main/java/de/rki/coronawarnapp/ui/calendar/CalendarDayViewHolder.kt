package de.rki.coronawarnapp.ui.calendar

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

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
     * Accessibility talk back date format
     */
    private val talkBackDateFormat = DateTimeFormat.forPattern("EEEE d MMMMM")

    /**
     * Bind data to view
     */
    fun bind(day: CalendarAdapter.Day, clickListener: (CalendarAdapter.Day) -> Unit) {
        val context = textView.context
        val today = LocalDate.now()

        // Set day text
        textView.text = day.date.dayOfMonth.toString()

        // Set day content description for talk back
        textView.contentDescription = day.date.toString(talkBackDateFormat)

        // If date is after today or exceeds 21 days in the past- then disable click listener
        if (!day.date.isAfter(today) && !day.date.isBefore(today.minusDays(ONSET_PERIOD))) {
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
            // Past > 21 days
            day.date.isBefore(today.minusDays(ONSET_PERIOD)) -> {
                textView.setBackgroundResource(0)
                textView.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary3))
            }
        }
    }

    companion object {
        // Max number of days for the onset of symptoms to be calculated
        private const val ONSET_PERIOD = 21
    }
}
