package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.TimeAndDateExtensions.calculateDays
import de.rki.coronawarnapp.util.TimeAndDateExtensions.getCurrentHourUTC
import io.mockk.MockKAnnotations
import io.mockk.mockkObject
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

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
