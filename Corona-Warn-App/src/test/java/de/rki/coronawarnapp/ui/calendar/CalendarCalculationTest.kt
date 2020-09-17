package de.rki.coronawarnapp.ui.calendar

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Assert
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
        Assert.assertEquals(3, dates.first().date.dayOfMonth)
        Assert.assertEquals(8, dates.first().date.monthOfYear)

        // Last day - 30 of August
        Assert.assertEquals(30, dates.last().date.dayOfMonth)
        Assert.assertEquals(8, dates.last().date.monthOfYear)

        val monthLabel = CalendarCalculation().getMonthText(dates.first().date, dates.last().date)
        Assert.assertEquals("August 2020", monthLabel)
    }

    @Test
    fun calculateSameYearDifferentMonth() {
        var input = "15.09.2020"
        val dateTime =
            DateTime.parse(input, DateTimeFormat.forPattern(pattern))
        val dates = CalendarCalculation().getDates(dateTime)

        // First day - 24 of August
        Assert.assertEquals(24, dates.first().date.dayOfMonth)
        Assert.assertEquals(8, dates.first().date.monthOfYear)

        // Last day - 20 of September
        Assert.assertEquals(20, dates.last().date.dayOfMonth)
        Assert.assertEquals(9, dates.last().date.monthOfYear)

        val monthLabel = CalendarCalculation().getMonthText(dates.first().date, dates.last().date)
        Assert.assertEquals("August - September 2020", monthLabel)
    }

    @Test
    fun calculateDifferentYearDifferentMonth() {
        var input = "12.01.2021"
        val dateTime =
            DateTime.parse(input, DateTimeFormat.forPattern(pattern))
        val dates = CalendarCalculation().getDates(dateTime)

        // First day - 21 of December 2020
        Assert.assertEquals(21, dates.first().date.dayOfMonth)
        Assert.assertEquals(12, dates.first().date.monthOfYear)
        Assert.assertEquals(2020, dates.first().date.year)

        // Last day - 17 of January 2021
        Assert.assertEquals(17, dates.last().date.dayOfMonth)
        Assert.assertEquals(1, dates.last().date.monthOfYear)
        Assert.assertEquals(2021, dates.last().date.year)

        val monthLabel = CalendarCalculation().getMonthText(dates.first().date, dates.last().date)
        Assert.assertEquals("December 2020 - January 2021", monthLabel)
    }
}
