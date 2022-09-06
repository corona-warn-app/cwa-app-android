package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.TimeAndDateExtensions.derive10MinutesInterval
import de.rki.coronawarnapp.util.TimeAndDateExtensions.deriveHourInterval
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

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
    fun test_daysAgo() {
        LocalDate.of(2012, 3, 4).ageInDays(
            LocalDate.of(2012, 3, 4)
        ) shouldBe 0

        LocalDate.of(2013, 12, 31).ageInDays(
            LocalDate.of(2014, 1, 2)
        ) shouldBe 2

        LocalDate.of(2014, 5, 2).ageInDays(
            LocalDate.of(2014, 5, 5)
        ) shouldBe 3
    }

    @Test
    fun `seconds to instant`() {
        2687955L.secondsToInstant() shouldBe org.joda.time.Instant.parse("1970-02-01T02:39:15.000Z")
    }

    @Test
    fun `0 seconds to instant`() {
        0L.secondsToInstant() shouldBe org.joda.time.Instant.parse("1970-01-01T00:00:00.000Z")
    }

    @Test
    fun `-10 seconds to instant`() {
        (-10).toLong().secondsToInstant() shouldBe org.joda.time.Instant.parse("1969-12-31T23:59:50.000Z")
    }

    @Test
    fun `derive 10 minutes interval`() {
        Instant.parse("2021-03-02T09:57:11.000Z").derive10MinutesInterval() shouldBe 2691125
    }

    @Test
    fun `derive 10 minutes interval should be 0`() {
        Instant.parse("1970-01-01T00:00:00.000Z").derive10MinutesInterval() shouldBe 0
    }

    @Test
    fun `derive 1 hour interval should be 0`() {
        Instant.parse("1970-01-01T00:00:00.000Z")
            .deriveHourInterval() shouldBe 0
    }

    @Test
    fun `derive 1 hour interval`() {
        Instant.parse("2021-02-15T13:52:05Z").deriveHourInterval() shouldBe 448165
    }

    @Test
    fun `toDateTimeAtStartOfDayUtc returns a date on the same day if converted to instant`() {
        val day = LocalDate.of(2021, 2, 15)
        val startOfDayUtc = day.atStartOfDay(ZoneOffset.UTC)
        val timeStampUtc = startOfDayUtc.toInstant().epochSecond
        val tsuInstantString = timeStampUtc.secondsToInstant().toString()

        tsuInstantString shouldBe "2021-02-15T00:00:00.000Z"
    }
}
