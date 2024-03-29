package de.rki.coronawarnapp.ui.calendar

import android.content.Context
import de.rki.coronawarnapp.contactdiary.util.getLocale
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class CalendarCalculation constructor(private val context: Context) {

    private val locale: Locale
        get() = context.getLocale()

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
    fun getMonthText(firstDate: LocalDate, lastDate: LocalDate): String {
        val monthText = StringBuilder()
        // Append first date month as it would always be displayed
        monthText.append(firstDate.month.getDisplayName(TextStyle.FULL, locale))
        if (firstDate.month != lastDate.month) {
            // Different month
            if (firstDate.year == lastDate.year) {
                // Same year (Case 1)
                monthText.append(" - ")
                    .append(lastDate.month.getDisplayName(TextStyle.FULL, locale))
            } else {
                // Different year (Case 2)
                monthText.append(" ")
                    .append(firstDate.year)
                    .append(" - ")
                    .append(lastDate.month.getDisplayName(TextStyle.FULL, locale))
            }
            // Append last date year
            monthText.append(" ")
                .append(lastDate.year)
        } else {
            // Same month
            monthText.append(" ")
                .append(firstDate.year)
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
     * Goal: calculate days to add with java.time to current date
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
    fun getDates(currentDate: LocalDate = LocalDate.now()): List<CalendarAdapter.Day> {
        // Create mutable list of DateTime as a result
        val result = mutableListOf<CalendarAdapter.Day>()
        // Get current day of the week (where 1 = Monday, 7 = Sunday)
        val currentDayOfTheWeek = currentDate.dayOfWeek.value
        // Week count
        val weeksCount = WEEKS_COUNT - 1
        for (weekId in 0..weeksCount) {
            for (dayId in 1..DAYS_IN_WEEK) {
                val daysDiff = (currentDayOfTheWeek * -1) + dayId - (DAYS_IN_WEEK * (weeksCount - weekId))
                result.add(CalendarAdapter.Day(currentDate.plusDays(daysDiff.toLong())))
            }
        }
        return result
    }

    companion object {
        /**
         * Total days in week
         */
        const val DAYS_IN_WEEK = 7

        /**
         * Weeks count
         */
        private const val WEEKS_COUNT = 4
    }
}
