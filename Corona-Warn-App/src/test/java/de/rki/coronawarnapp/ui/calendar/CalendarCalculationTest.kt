package de.rki.coronawarnapp.ui.calendar

import io.kotest.matchers.shouldBe
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Test

class CalendarCalculationTest {

    private var pattern = "dd.MM.yyyy"
    private val formatter = DateTimeFormat.forPattern(pattern)

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

    @Test
    fun calculateEdgeCases() {
        // new year
        CalendarCalculation().getDates(DateTime.parse("27.12.2021", formatter)).apply {
            // First day - 6 of December 2021
            first().date.dayOfMonth shouldBe 6
            first().date.monthOfYear shouldBe 12
            first().date.year shouldBe 2021

            // Last day - 2 of January 2022
            last().date.dayOfMonth shouldBe 2
            last().date.monthOfYear shouldBe 1
            last().date.year shouldBe 2022

            CalendarCalculation().getMonthText(
                first().date,
                last().date
            ) shouldBe "December 2021 - January 2022"
        }

        // leap year
        CalendarCalculation().getDates(DateTime.parse("29.02.2024", formatter)).apply {
            // First day - 5 of February 2024
            first().date.dayOfMonth shouldBe 5
            first().date.monthOfYear shouldBe 2
            first().date.year shouldBe 2024

            // Last day - 2 of March 2024
            last().date.dayOfMonth shouldBe 3
            last().date.monthOfYear shouldBe 3
            last().date.year shouldBe 2024

            CalendarCalculation().getMonthText(
                first().date,
                last().date
            ) shouldBe "February - March 2024"
        }
    }
}
