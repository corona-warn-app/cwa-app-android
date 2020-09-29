package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.TimeAndDateExtensions.numberOfDayChanges
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
import org.junit.Assert
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

    @Test
    fun test_daysAgo() {
        Assert.assertEquals(
            0, numberOfDayChanges(
                DateTime(2012, 3, 4, 1, 2),
                DateTime(2012, 3, 4, 16, 9)
            )
        )
        Assert.assertEquals(
            2, numberOfDayChanges(
                DateTime(2013, 12, 31, 1, 2),
                DateTime(2014, 1, 2, 16, 9)
            )
        )
        Assert.assertEquals(
            3, numberOfDayChanges(
                DateTime(2014, 5, 2, 17, 2),
                DateTime(2014, 5, 5, 4, 9)
            )
        )
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
