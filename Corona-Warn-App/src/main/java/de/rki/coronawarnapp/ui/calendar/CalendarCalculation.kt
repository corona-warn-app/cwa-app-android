package de.rki.coronawarnapp.ui.calendar

import dagger.Reusable
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.joda.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Reusable
class CalendarCalculation @Inject constructor() {

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
    fun getDates(currentDate: DateTime = DateTime(Instant.now(), DateTimeZone.UTC)): List<CalendarAdapter.Day> {
        // Create mutable list of DateTime as a result
        val result = mutableListOf<CalendarAdapter.Day>()
        // Get current day of the week (where 1 = Monday, 7 = Sunday)
        val currentDayOfTheWeek = currentDate.dayOfWeek().get()
        // Week count
        val weeksCount = WEEKS_COUNT - 1
        for (weekId in 0..weeksCount) {
            for (dayId in 1..DAYS_IN_WEEK) {
                val daysDiff = (currentDayOfTheWeek * -1) + dayId - (DAYS_IN_WEEK * (weeksCount - weekId))
                result.add(CalendarAdapter.Day(currentDate.plusDays(daysDiff).toLocalDate()))
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
