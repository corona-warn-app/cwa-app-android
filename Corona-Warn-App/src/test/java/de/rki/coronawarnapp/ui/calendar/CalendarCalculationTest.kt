package de.rki.coronawarnapp.ui.calendar

import junit.framework.TestCase
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Test

class CalendarCalculationTest {

    private var pattern = "dd.MM.yyyy"

    @Test
    fun calculateSameYearSameMonth() {
        var input = "27.08.2020"
        val dateTime =
            DateTime.parse(input, DateTimeFormat.forPattern(pattern))
        val dates = CalendarCalculation().getDates(dateTime)

        // First day - 3 of August
        TestCase.assertEquals(3, dates.first().date.dayOfMonth)
        TestCase.assertEquals(8, dates.first().date.monthOfYear)

        // Last day - 30 of August
        TestCase.assertEquals(30, dates.last().date.dayOfMonth)
        TestCase.assertEquals(8, dates.last().date.monthOfYear)

        val monthLabel = CalendarCalculation().getMonthText(dates.first().date, dates.last().date)
        TestCase.assertEquals("August 2020", monthLabel)
    }

    @Test
    fun calculateSameYearDifferentMonth() {
        var input = "15.09.2020"
        val dateTime =
            DateTime.parse(input, DateTimeFormat.forPattern(pattern))
        val dates = CalendarCalculation().getDates(dateTime)

        // First day - 24 of August
        TestCase.assertEquals(24, dates.first().date.dayOfMonth)
        TestCase.assertEquals(8, dates.first().date.monthOfYear)

        // Last day - 20 of September
        TestCase.assertEquals(20, dates.last().date.dayOfMonth)
        TestCase.assertEquals(9, dates.last().date.monthOfYear)

        val monthLabel = CalendarCalculation().getMonthText(dates.first().date, dates.last().date)
        TestCase.assertEquals("August - September 2020", monthLabel)
    }

    @Test
    fun calculateDifferentYearDifferentMonth() {
        var input = "12.01.2021"
        val dateTime =
            DateTime.parse(input, DateTimeFormat.forPattern(pattern))
        val dates = CalendarCalculation().getDates(dateTime)

        // First day - 21 of December 2020
        TestCase.assertEquals(21, dates.first().date.dayOfMonth)
        TestCase.assertEquals(12, dates.first().date.monthOfYear)
        TestCase.assertEquals(2020, dates.first().date.year)

        // Last day - 17 of January 2021
        TestCase.assertEquals(17, dates.last().date.dayOfMonth)
        TestCase.assertEquals(1, dates.last().date.monthOfYear)
        TestCase.assertEquals(2021, dates.last().date.year)

        val monthLabel = CalendarCalculation().getMonthText(dates.first().date, dates.last().date)
        TestCase.assertEquals("December 2020 - January 2021", monthLabel)
    }
}
