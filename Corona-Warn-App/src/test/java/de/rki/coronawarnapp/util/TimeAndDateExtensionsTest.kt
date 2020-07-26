package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.TimeAndDateExtensions.calculateDays
import de.rki.coronawarnapp.util.TimeAndDateExtensions.calculateIfGivenTimeIsNewDay
import de.rki.coronawarnapp.util.TimeAndDateExtensions.getCurrentHourUTC
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * TimeAndDateExtensions test.
 */

class TimeAndDateExtensionsTest {

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CoronaWarnApplication)
        mockkObject(TimeAndDateExtensions)
        mockkStatic(DateTimeZone::class)
        every { DateTimeZone.getDefault() } returns DateTimeZone.forOffsetHours(2)
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

    private fun assertNewDay(nowDateIso: String, referenceDateIso: String, isNewDay: Boolean) {
        val now = Instant.parse(nowDateIso)
        val reference = Instant.parse(referenceDateIso).toDate()

        val result = calculateIfGivenTimeIsNewDay(now, reference)
        MatcherAssert.assertThat(result, CoreMatchers.`is`(isNewDay))
    }

    @Test
    fun calculateIfGivenTimeIsNewDayTest() {
        // only local time
        every { TimeAndDateExtensions["doesQuotaResetAtMidnightUTC"]() } returns false
        every { TimeAndDateExtensions["doesQuotaResetAtMidnightLocalTime"]() } returns true

        assertNewDay("2020-01-02T01:00:00.00+02", "2020-01-01T15:00:00.00+02", true)
        assertNewDay("2020-01-02T03:00:00.00+02", "2020-01-01T15:00:00.00+02", true)
        assertNewDay("2020-01-02T03:00:00.00+02", "2020-01-02T01:00:00.00+02", false)

        // only UTC time
        every { TimeAndDateExtensions["doesQuotaResetAtMidnightUTC"]() } returns true
        every { TimeAndDateExtensions["doesQuotaResetAtMidnightLocalTime"]() } returns false

        assertNewDay("2020-01-02T01:00:00.00+02", "2020-01-01T15:00:00.00+02", false)
        assertNewDay("2020-01-02T03:00:00.00+02", "2020-01-01T15:00:00.00+02", true)
        assertNewDay("2020-01-02T03:00:00.00+02", "2020-01-02T01:00:00.00+02", true)

        // both UTC and local time
        every { TimeAndDateExtensions["doesQuotaResetAtMidnightUTC"]() } returns true
        every { TimeAndDateExtensions["doesQuotaResetAtMidnightLocalTime"]() } returns true

        assertNewDay("2020-01-02T01:00:00.00+02", "2020-01-01T15:00:00.00+02", false)
        assertNewDay("2020-01-02T03:00:00.00+02", "2020-01-01T15:00:00.00+02", true)
        assertNewDay("2020-01-02T03:00:00.00+02", "2020-01-02T01:00:00.00+02", false)
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
