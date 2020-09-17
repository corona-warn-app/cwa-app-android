package de.rki.coronawarnapp.ui.calendar

import io.kotest.matchers.shouldBe
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
        dates.first().date.dayOfMonth shouldBe 3
        dates.first().date.monthOfYear shouldBe 8

        // Last day - 30 of August
        dates.last().date.dayOfMonth shouldBe 30
        dates.last().date.monthOfYear shouldBe 8

        CalendarCalculation().getMonthText(
            dates.first().date,
            dates.last().date
        ) shouldBe "August 2020"
    }

    @Test
    fun calculateSameYearDifferentMonth() {
        var input = "15.09.2020"
        val dateTime =
            DateTime.parse(input, DateTimeFormat.forPattern(pattern))
        val dates = CalendarCalculation().getDates(dateTime)

        // First day - 24 of August
        dates.first().date.dayOfMonth shouldBe 24
        dates.first().date.monthOfYear shouldBe 8

        // Last day - 20 of September
        dates.last().date.dayOfMonth shouldBe 20
        dates.last().date.monthOfYear shouldBe 9

        CalendarCalculation().getMonthText(
            dates.first().date,
            dates.last().date
        ) shouldBe "August - September 2020"
    }

    @Test
    fun calculateDifferentYearDifferentMonth() {
        var input = "12.01.2021"
        val dateTime =
            DateTime.parse(input, DateTimeFormat.forPattern(pattern))
        val dates = CalendarCalculation().getDates(dateTime)

        // First day - 21 of December 2020
        dates.first().date.dayOfMonth shouldBe 21
        dates.first().date.monthOfYear shouldBe 12
        dates.first().date.year shouldBe 2020

        // Last day - 17 of January 2021
        dates.last().date.dayOfMonth shouldBe 17
        dates.last().date.monthOfYear shouldBe 1
        dates.last().date.year shouldBe 2021

        CalendarCalculation().getMonthText(
            dates.first().date,
            dates.last().date
        ) shouldBe "December 2020 - January 2021"
    }
}
