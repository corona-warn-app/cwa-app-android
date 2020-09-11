package de.rki.coronawarnapp.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import org.joda.time.LocalDate

/**
 * Calendar adapter for recycler view
 *
 * @param clickListener (Day) -> Unit - on item click event listener
 */
class CalendarAdapter (private val clickListener:(Day) -> Unit) :
    RecyclerView.Adapter<CalendarDayViewHolder>() {

    /**
     * Mutable list of days
     *
     * @see Day
     */
    private val data = mutableListOf<Day>()

    init {
        setHasStableIds(true)
    }

    /**
     * Create new calendar day view holders
     *
     * @see CalendarDayViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CalendarDayViewHolder {
        // Create a new view.
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.fragment_calendar_day, viewGroup, false)

        return CalendarDayViewHolder(v)
    }

    /**
     * Update calendar day view holders
     *
     * @see CalendarDayViewHolder.bind
     */
    override fun onBindViewHolder(viewHolder: CalendarDayViewHolder, position: Int) {
        viewHolder.bind(data[position], clickListener)
    }

    /**
     * Update days list and notify that data set was changed
     *
     * @see CalendarDayViewHolder.bind
     */
    fun update(days: List<Day>) {
        data.clear()
        data.addAll(days)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * Data class for calendar day
     *
     * @param date LocalDate
     * @param isSelected Boolean
     *
     * @see LocalDate
     */
    data class Day(val date:LocalDate, val isSelected:Boolean = false)
}
