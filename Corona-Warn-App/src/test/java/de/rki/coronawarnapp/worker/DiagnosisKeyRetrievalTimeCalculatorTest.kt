package de.rki.coronawarnapp.worker

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.hamcrest.core.IsEqual.equalTo
import org.joda.time.DateTime
import org.junit.Test

class DiagnosisKeyRetrievalTimeCalculatorTest {

    @Test
    fun getDatetimeRangeForDelayInWindow() {
        val timeNow = DateTime()
            .withHourOfDay(8)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)

        val timeNowPlusMaxDelay =
            timeNow.plusMinutes(BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_MAX_DELAY)

        val result = DiagnosisKeyRetrievalTimeCalculator.getPossibleSchedulingTimes(timeNow)

        val todayMidnight = DateTime().withTimeAtStartOfDay()
        val windowMin = todayMidnight.plusMinutes(BackgroundConstants.TIME_RANGE_MIN)
        val windowMax = todayMidnight.plusMinutes(BackgroundConstants.TIME_RANGE_MAX)

        assertThat(result.first, `is`(equalTo(timeNow)))
        assertThat(result.second, `is`(equalTo(timeNowPlusMaxDelay)))

        assertThat(result.first, `is`(greaterThanOrEqualTo(windowMin)))
        assertThat(result.second, `is`(lessThanOrEqualTo(windowMax)))
    }

    @Test
    fun getDatetimeRangeForDelayInWindowStartTimeBeforeWindow() {

        val timeNow = DateTime()
            .withHourOfDay(5)
            .withMinuteOfHour(20)
            .withSecondOfMinute(0)
        val result = DiagnosisKeyRetrievalTimeCalculator.getPossibleSchedulingTimes(timeNow)

        val todayMidnight = DateTime().withTimeAtStartOfDay()
        val windowMin = todayMidnight.plusMinutes(BackgroundConstants.TIME_RANGE_MIN)
        val windowMinPlusTimeRangeMax =
            windowMin.plusMinutes(BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_MAX_DELAY)

        assertThat(result.first, `is`(equalTo(windowMin)))
        assertThat(result.second, `is`(lessThanOrEqualTo(windowMinPlusTimeRangeMax)))
    }

    @Test
    fun getDatetimeRangeForDelayInWindowEndimeAfterWindow() {
        val timeNow = DateTime()
            .withHourOfDay(23)
            .withMinuteOfHour(20)
            .withSecondOfMinute(0)
        val result = DiagnosisKeyRetrievalTimeCalculator.getPossibleSchedulingTimes(timeNow)

        val todayMidnight = DateTime().withTimeAtStartOfDay()
        val windowMax = todayMidnight.plusMinutes(BackgroundConstants.TIME_RANGE_MAX)

        assertThat(result.first, `is`(equalTo(timeNow)))
        assertThat(result.second, `is`(lessThanOrEqualTo(windowMax)))
    }
}
