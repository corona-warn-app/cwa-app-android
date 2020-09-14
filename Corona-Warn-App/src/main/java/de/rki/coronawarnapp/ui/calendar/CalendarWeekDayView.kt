package de.rki.coronawarnapp.ui.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import de.rki.coronawarnapp.R

/**
 * Week day custom view
 */
class CalendarWeekDayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val textView: TextView

    /**
     * Initialize the view
     *
     * Get TextView for day setup
     * SetUp layout params
     */
    init {
        LayoutInflater.from(context)
            .inflate(R.layout.fragment_calendar_day, this, true)
        textView = findViewById(R.id.dayText)

        layoutParams = LayoutParams(
            0,
            LayoutParams.WRAP_CONTENT,
            1.0f
        )
    }

    /**
     * SetUp the view from CalendarFragment
     */
    fun setUp(text: String, isSelected: Boolean = false) {
        textView.text = text

        if (isSelected) {
            textView.setTextAppearance(R.style.calendarWeekDaySelected)
        } else {
            textView.setTextAppearance(R.style.calendarWeekDayNormal)
        }
    }
}
