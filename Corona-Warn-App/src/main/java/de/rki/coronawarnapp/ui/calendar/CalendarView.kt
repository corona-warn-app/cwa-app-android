package de.rki.coronawarnapp.ui.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.Locale
import kotlin.text.StringBuilder

/**
 * Custom calendar view with rules:
 * - 4 Weeks
 * - 7 Days
 * - Current week - last row
 * - Week starts from Monday
 */
class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        /**
         * Zero
         */
        private const val ZERO = 0

        /**
         * First day of the week
         */
        private const val FIRST_DAY = 1

        /**
         * Total days in week
         */
        private const val DAYS_IN_WEEK = 7

        /**
         * Weeks count
         */
        private const val WEEKS_COUNT = 4

        /**
         * Day of week text length
         * 3 = Mon
         * 1 = M
         */
        private const val DAY_OF_WEEK_TEXT_LENGTH = 1

        /**
         * Shift for logic
         */
        private const val SHIFT = -1
    }

    /**
     * Calendar layout
     */
    private var calendarLayout: LinearLayout

    /**
     * Calendar header
     */
    private var headerTextView: TextView

    /**
     * Recycler view for dates
     *
     * @see RecyclerView
     */
    private var recyclerView: RecyclerView

    /**
     * Layout manager for recycler view
     *
     * @see RecyclerView.LayoutManager
     */
    private var layoutManager: RecyclerView.LayoutManager

    /**
     * Mutable list of Day
     *
     * @see CalendarAdapter.Day
     */
    private val days = mutableListOf<CalendarAdapter.Day>()

    /**
     * Recycler view adapter
     *
     * @see CalendarAdapter
     */
    private lateinit var adapter: CalendarAdapter

    /**
     * On item click event listener
     *
     * @see CalendarAdapter.update
     * @see updateHeader
     */
    private val onItemClickListener: (CalendarAdapter.Day) -> Unit = { selectedDay ->
        // Update data set
        val updateData = days.map { oldDay -> oldDay.copy(isSelected = selectedDay == oldDay) }
        // Update selection
        updateSelection(updateData.any { it.isSelected })

        adapter.update(updateData)
    }

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.fragment_calendar, this, true)

        // Get linear layout
        calendarLayout = findViewById<LinearLayout>(R.id.calendar_layout)

        // Get header view
        headerTextView = findViewById<TextView>(R.id.calendar_header)

        // Get recycler view
        recyclerView = findViewById<RecyclerView>(R.id.calendar_recycler_view)

        // Create layout manager
        layoutManager = LinearLayoutManager(context)
        // Set to grid layout
        layoutManager = GridLayoutManager(context, DAYS_IN_WEEK)

        with(recyclerView) {
            layoutManager = this@CalendarView.layoutManager
            scrollToPosition(ZERO)
        }

        // Calculate dates to display
        days.addAll(getDates())

        // Set calendar adapter as adapter for recycler view
        adapter = CalendarAdapter(onItemClickListener)
        adapter.update(days)

        recyclerView.adapter = adapter

        // Setup day legend
        setUpDayLegend(this)

        // Setup month
        setUpMonthTextView(this)
    }

    /**
     * Update header and top level layout background
     */
    private fun updateSelection(isSelected: Boolean) {
        calendarLayout.isSelected = isSelected
        headerTextView.isSelected = isSelected
    }

    /**
     * SetUp day legend (week day)
     *
     * NOTE: DaysOfWeek is impossible to use due to API 23
     *
     * @param view View - CalendarView
     *
     * @see getMonthText
     */
    private fun setUpDayLegend(view: View) {
        // Get day legend layout
        val dayLegendLayout = findViewById<LinearLayout>(R.id.calendar_day_legend)
        // Get current week day
        val date = LocalDate()
        val currentWeekDay = DateTime(Instant.now()).dayOfWeek().get()
        for (dayId in FIRST_DAY..DAYS_IN_WEEK) {
            val dayOfWeek = CalendarWeekDayView(context)
            val weekDay = date.withDayOfWeek(dayId).dayOfWeek()
            // weekDay.getAsText returns in either "Fri" or "Friday" format, substring first latter
            dayOfWeek.setUp(weekDay.getAsText(Locale.getDefault()).take(DAY_OF_WEEK_TEXT_LENGTH),
                weekDay.get() == currentWeekDay)
            dayLegendLayout.addView(dayOfWeek)
        }
    }

    /**
     * SetUp month text view
     *
     * @param view View - CalendarView
     *
     * @see getMonthText
     */
    private fun setUpMonthTextView(view: View) {
        // Get month text view
        val monthTextView = findViewById<TextView>(R.id.calendar_month)

        // Get first and last days
        val firstDate = days.first().date
        val lastDate = days.last().date

        monthTextView.text = getMonthText(firstDate, lastDate)
    }

    /**
     * Get month text view text
     *
     * Algorithm:
     * Case 1:
     * first date month != last date month
     * first date year == last date year
     * Result: September - October 2020
     *
     * Case 2:
     * first date month != last date month
     * first date year != last date year
     * Result: December 2020 - January 2021
     *
     * Case 3:
     * first date month == last date month
     * Result: September 2020
     *
     * NOTE: This algorithm does not cover same month, but different year - calendar has
     * strict constant of 28 days to display, so this case would never happen.
     *
     * @param firstDate LocalDate - first displayed date
     * @param lastDate LocalDate - last displayed date
     *
     * @return String
     *
     * @see StringBuilder
     */
    private fun getMonthText(firstDate: LocalDate, lastDate: LocalDate): String {
        val monthText = StringBuilder()
        // Append first date month as it would always be displayed
        monthText.append(firstDate.monthOfYear().getAsText(Locale.getDefault()))
        if (firstDate.monthOfYear() != lastDate.monthOfYear()) {
            // Different month
            if (firstDate.year() == lastDate.year()) {
                // Same year (Case 1)
                monthText.append(" - ")
                    .append(lastDate.monthOfYear().getAsText(Locale.getDefault()))
            } else {
                // Different year (Case 2)
                monthText.append(" ")
                    .append(firstDate.year().get())
                    .append(" - ")
                    .append(lastDate.monthOfYear().getAsText(Locale.getDefault()))
            }
            // Append last date year
            monthText.append(" ")
                .append(lastDate.year().get())
        } else {
            // Same month
            monthText.append(" ")
                .append(firstDate.year().get())
        }
        return monthText.toString()
    }

    /**
     * Calculate dates for calendar
     * Input constants:
     * - 4 Weeks (TotalWeeks)
     * - 7 Days (DaysInWeekCount)
     * - Current week - last row
     * - Week starts from Monday
     *
     * Algorithm:
     * Goal: calculate days to add with JodaTime lib to current date
     *
     * Input: Today = 9 September (Wednesday)
     *
     * Step 1: Define day shift in the week
     * |_M_|_T_|_W_|_T_|_F_|_S_|_S_|
     * | -2| -1| 9 | +1| +2| +3| +4| <- Current Week (4th row)
     * Code: (CurrentDayOfTheWeek * -1) + dayId
     *
     * Step 2: Apply week shift
     * |_M_|_T_|_W_|_T_|_F_|_S_|_S_|
     * | -9| -8| -7| -6| -5| -4| -3| <- Previous Week (3d row)
     * | -2| -1| 9 | +1| +2| +3| +4| <- Current Week (4th row)
     * Code: (DaysInWeekCount * (TotalWeeks - weekId)) * -1
     */
    private fun getDates(): List<CalendarAdapter.Day> {
        // Create mutable list of DateTime as a result
        val result = mutableListOf<CalendarAdapter.Day>()
        // Get current date. We do not bound to UTC timezone
        val currentDate = DateTime(Instant.now())
        // Get current day of the week (where 1 = Monday, 7 = Sunday)
        val currentDayOfTheWeek = currentDate.dayOfWeek().get()
        // Week count
        val weeksCount = WEEKS_COUNT + SHIFT
        for (weekId in ZERO..weeksCount) {
            for (dayId in FIRST_DAY..DAYS_IN_WEEK) {
                val daysDiff = (currentDayOfTheWeek * SHIFT) + dayId - (DAYS_IN_WEEK * (weeksCount - weekId))
                result.add(CalendarAdapter.Day(currentDate.plusDays(daysDiff).toLocalDate()))
            }
        }
        return result
    }
}
