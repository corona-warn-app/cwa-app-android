package de.rki.coronawarnapp.ui.calendar

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Locale

class CalendarCalculationTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var resources: Resources
    @MockK lateinit var configuration: Configuration

    private var pattern = "dd.MM.yyyy"
    private val formatter = DateTimeFormat.forPattern(pattern)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.resources } returns resources
        every { resources.configuration } returns Configuration().apply {
            locale = Locale.ENGLISH
        }
    }

    fun createInstance() = CalendarCalculation(context)

    @Test
    fun calculateSameYearSameMonth() {
        val input = "27.08.2020"
        val dateTime =
            DateTime.parse(input, DateTimeFormat.forPattern(pattern))
        val dates = createInstance().getDates(dateTime)

        // First day - 3 of August
        dates.first().date.dayOfMonth shouldBe 3
        dates.first().date.monthOfYear shouldBe 8

        // Last day - 30 of August
        dates.last().date.dayOfMonth shouldBe 30
        dates.last().date.monthOfYear shouldBe 8

        createInstance().getMonthText(
            dates.first().date,
            dates.last().date
        ) shouldBe "August 2020"
    }

    @Test
    fun calculateSameYearDifferentMonth() {
        val input = "15.09.2020"
        val dateTime =
            DateTime.parse(input, DateTimeFormat.forPattern(pattern))
        val dates = createInstance().getDates(dateTime)

        // First day - 24 of August
        dates.first().date.dayOfMonth shouldBe 24
        dates.first().date.monthOfYear shouldBe 8

        // Last day - 20 of September
        dates.last().date.dayOfMonth shouldBe 20
        dates.last().date.monthOfYear shouldBe 9

        createInstance().getMonthText(
            dates.first().date,
            dates.last().date
        ) shouldBe "August - September 2020"
    }

    @Test
    fun calculateDifferentYearDifferentMonth() {
        val input = "12.01.2021"
        val dateTime =
            DateTime.parse(input, DateTimeFormat.forPattern(pattern))
        val dates = createInstance().getDates(dateTime)

        // First day - 21 of December 2020
        dates.first().date.dayOfMonth shouldBe 21
        dates.first().date.monthOfYear shouldBe 12
        dates.first().date.year shouldBe 2020

        // Last day - 17 of January 2021
        dates.last().date.dayOfMonth shouldBe 17
        dates.last().date.monthOfYear shouldBe 1
        dates.last().date.year shouldBe 2021

        createInstance().getMonthText(
            dates.first().date,
            dates.last().date
        ) shouldBe "December 2020 - January 2021"
    }

    @Test
    fun calculateEdgeCases() {
        // new year
        createInstance().getDates(DateTime.parse("27.12.2021", formatter)).apply {
            // First day - 6 of December 2021
            first().date.dayOfMonth shouldBe 6
            first().date.monthOfYear shouldBe 12
            first().date.year shouldBe 2021

            // Last day - 2 of January 2022
            last().date.dayOfMonth shouldBe 2
            last().date.monthOfYear shouldBe 1
            last().date.year shouldBe 2022

            createInstance().getMonthText(
                first().date,
                last().date
            ) shouldBe "December 2021 - January 2022"
        }

        // leap year
        createInstance().getDates(DateTime.parse("29.02.2024", formatter)).apply {
            // First day - 5 of February 2024
            first().date.dayOfMonth shouldBe 5
            first().date.monthOfYear shouldBe 2
            first().date.year shouldBe 2024

            // Last day - 2 of March 2024
            last().date.dayOfMonth shouldBe 3
            last().date.monthOfYear shouldBe 3
            last().date.year shouldBe 2024

            createInstance().getMonthText(
                first().date,
                last().date
            ) shouldBe "February - March 2024"
        }
    }
}
