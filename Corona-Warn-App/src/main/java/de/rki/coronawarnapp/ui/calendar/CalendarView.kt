package de.rki.coronawarnapp.ui.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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
     * Fragment click listener
     */
    private var listener: ((LocalDate?) -> Unit)? = null

    /**
     * On item click event listener
     *
     * @see CalendarAdapter.update
     * @see updateSelection
     */
    private val onItemClickListener: (CalendarAdapter.Day) -> Unit = { selectedDay ->
        // Update data set
        val updateData = days.map { oldDay -> oldDay.copy(isSelected = selectedDay == oldDay) }
        // Update selection
        updateSelection(updateData.any { it.isSelected })

        adapter.update(updateData)

        // Invoke fragment on click
        listener?.invoke(updateData.find { it.isSelected }?.date)
    }

    /**
     * Unset selection of each date shown
     *
     * @see CalendarAdapter.update
     */
    fun unsetSelection() {
        val updateData = days.map { oldDay -> oldDay.copy(isSelected = false) }
        updateSelection(false)
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
        layoutManager = GridLayoutManager(context, CalendarCalculation.DAYS_IN_WEEK)

        with(recyclerView) {
            layoutManager = this@CalendarView.layoutManager
            scrollToPosition(0)
        }

        // Calculate dates to display
        days.addAll(CalendarCalculation().getDates())

        // Set calendar adapter as adapter for recycler view
        adapter = CalendarAdapter(onItemClickListener)
        adapter.update(days)

        recyclerView.adapter = adapter

        // Setup day legend
        setUpDayLegend()

        // Setup month
        setUpMonthTextView()
    }

    /**
     * Set fragment click listener
     *
     * @see listener
     */
    fun setDateSelectedListener(listener: (LocalDate?) -> Unit) {
        this.listener = listener
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
     */
    private fun setUpDayLegend() {
        // Get day legend layout
        val dayLegendLayout = findViewById<LinearLayout>(R.id.calendar_day_legend)
        // Get current week day
        val date = LocalDate()
        val currentWeekDay = DateTime(Instant.now()).dayOfWeek().get()
        for (dayId in 1..CalendarCalculation.DAYS_IN_WEEK) {
            val dayOfWeek = CalendarWeekDayView(context)
            val weekDay = date.withDayOfWeek(dayId).dayOfWeek()
            // weekDay.getAsText returns in either "Fri" or "Friday" format, substring first latter
            dayOfWeek.setUp(
                weekDay.getAsText(Locale.getDefault()).take(1),
                weekDay.get() == currentWeekDay
            )
            dayLegendLayout.addView(dayOfWeek)
        }
    }

    /**
     * SetUp month text view
     *
     * @see CalendarCalculation.getMonthText
     */
    private fun setUpMonthTextView() {
        // Get month text view
        val monthTextView = findViewById<TextView>(R.id.calendar_month)

        // Get first and last days
        val firstDate = days.first().date
        val lastDate = days.last().date

        monthTextView.text = CalendarCalculation().getMonthText(firstDate, lastDate)
    }
}
