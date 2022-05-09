package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.TimeAndDateExtensions.ageInDays
import de.rki.coronawarnapp.util.TimeAndDateExtensions.daysUntil
import de.rki.coronawarnapp.util.TimeAndDateExtensions.derive10MinutesInterval
import de.rki.coronawarnapp.util.TimeAndDateExtensions.deriveHourInterval
import de.rki.coronawarnapp.util.TimeAndDateExtensions.getCurrentHourUTC
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDateTimeAtStartOfDayUtc
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

    @Test
    fun `seconds to instant`() {
        2687955L.secondsToInstant() shouldBe Instant.parse("1970-02-01T02:39:15.000Z")
    }

    @Test
    fun `0 seconds to instant`() {
        0L.secondsToInstant() shouldBe Instant.parse("1970-01-01T00:00:00.000Z")
    }

    @Test
    fun `-10 seconds to instant`() {
        (-10).toLong().secondsToInstant() shouldBe Instant.parse("1969-12-31T23:59:50.000Z")
    }

    @Test
    fun `derive 10 minutes interval`() {
        Instant.parse("2021-03-02T09:57:11+01:00")
            .derive10MinutesInterval() shouldBe 2691125
    }

    @Test
    fun `derive 10 minutes interval should be 0`() {
        Instant.parse("1970-01-01T00:00:00.000Z")
            .derive10MinutesInterval() shouldBe 0
    }

    @Test
    fun `derive 1 hour interval should be 0`() {
        Instant.parse("1970-01-01T00:00:00.000Z")
            .deriveHourInterval() shouldBe 0
    }

    @Test
    fun `derive 1 hour interval`() {
        Instant.parse("2021-02-15T13:52:05+00:00")
            .deriveHourInterval() shouldBe 448165
    }

    @Test
    fun `days until`() {
        Instant.parse("2021-02-15T13:52:05+00:00").daysUntil(
            date = Instant.parse("2021-02-15T13:52:05+02:00"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe 0

        Instant.parse("2021-02-13T23:00:05+00:00").daysUntil(
            date = Instant.parse("2021-02-15T00:52:05+02:00"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe 1

        Instant.parse("2021-02-15T00:00:05+00:00").daysUntil(
            date = Instant.parse("2021-02-14T00:52:05+02:00"),
            timeZone = DateTimeZone.forOffsetHours(2)
        ) shouldBe -1

        Instant.parse("2021-02-15T00:00:05+02:00").daysUntil(
            date = Instant.parse("2021-02-14T00:52:05+00:00"),
            timeZone = DateTimeZone.forOffsetHours(0)
        ) shouldBe 0
    }

    @Test
    fun `toDateTimeAtStartOfDayUtc returns a date on the same day if converted to instant`() {
        val day = LocalDate(2021, 2, 15)
        val startOfDayUtc = day.toDateTimeAtStartOfDayUtc()
        val startOfDay = day.toDateTimeAtStartOfDay()

        val timeStampUtc = startOfDayUtc.toInstant().seconds
        val timeStamp = startOfDay.toInstant().seconds

        val tsuInstantString = timeStampUtc.secondsToInstant().toString()
        val tsInstantString = timeStamp.secondsToInstant().toString()

        tsuInstantString shouldBe "2021-02-15T00:00:00.000Z"
        tsInstantString shouldBe "2021-02-14T23:00:00.000Z"
    }
}
