package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.TimeAndDateExtensions.ageInDays
import de.rki.coronawarnapp.util.TimeAndDateExtensions.calculateDays
import de.rki.coronawarnapp.util.TimeAndDateExtensions.getCurrentHourUTC
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.mockkObject
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.concurrent.TimeUnit

/**
 * TimeAndDateExtensions test.
 */

class TimeAndDateExtensionsTest : BaseTest() {

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication)
    }

    @Test
    fun getCurrentHourUTCTest() {
        val result = getCurrentHourUTC()
        MatcherAssert.assertThat(result, CoreMatchers.`is`(DateTime(Instant.now(), DateTimeZone.UTC).hourOfDay().get()))
    }

    @Test
    fun calculateDaysTest() {
        val lFirstDate = DateTime(2019, 1, 1, 1, 1).millis
        val lSecondDate = DateTime(2020, 1, 1, 1, 1).millis

        val result = calculateDays(firstDate = lFirstDate, secondDate = lSecondDate)
        MatcherAssert.assertThat(result, CoreMatchers.`is`(TimeUnit.MILLISECONDS.toDays(lSecondDate - lFirstDate)))
    }

    @Test
    fun test_daysAgo() {
        LocalDate(2012, 3, 4).ageInDays(
            LocalDate(2012, 3, 4)
        ) shouldBe 0

        LocalDate(2013, 12, 31).ageInDays(
            LocalDate(2014, 1, 2)
        ) shouldBe 2

        LocalDate(2014, 5, 2).ageInDays(
            LocalDate(2014, 5, 5)
        ) shouldBe 3
    }

    @Test
    fun `instant seconds extension`() {
        Instant.ofEpochMilli(-1).seconds shouldBe 0
        Instant.ofEpochMilli(0).seconds shouldBe 0
        Instant.ofEpochMilli(999).seconds shouldBe 0
        Instant.ofEpochMilli(1000).seconds shouldBe 1
        Instant.ofEpochMilli(1999).seconds shouldBe 1
        Instant.ofEpochMilli(2000).seconds shouldBe 2
    }
}
