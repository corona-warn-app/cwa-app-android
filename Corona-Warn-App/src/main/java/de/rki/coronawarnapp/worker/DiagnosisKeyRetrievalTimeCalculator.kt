package de.rki.coronawarnapp.worker

import org.joda.time.DateTime
import org.joda.time.Duration
import kotlin.random.Random

object DiagnosisKeyRetrievalTimeCalculator {

    /**
     * Generates a random duration for scheduling a job
     *
     * TODO check how to simplify and check edge cases
     *
     * @param currentTime
     *
     * @return random duration to schedule job
     *
     * @see getPossibleSchedulingTimes
     */
    fun generateDiagnosisKeyRetrievalOneTimeWorkRandomDuration(currentTime: DateTime): Long {
        val rangeForDelay = getPossibleSchedulingTimes(currentTime)

        val minMinutesOfDelay = convertTimestampToMinutesFromNow(rangeForDelay.first, currentTime)
        val maxMinutesOfDelay = convertTimestampToMinutesFromNow(rangeForDelay.second, currentTime)

        if (minMinutesOfDelay == maxMinutesOfDelay) {
            return minMinutesOfDelay.toLong()
        }
        return Random.nextInt(minMinutesOfDelay, maxMinutesOfDelay).toLong()
    }

    /**
     * Get the earliest and latest possible scheduling time for a job based on BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_MIN_DELAY and BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_MAX_DELAY
     * The delay should be generated within BackgroundConstants.TIME_RANGE_MIN and BackgroundConstants.TIME_RANGE_MAX
     *
     * Cases:
     *
     * Case 1: if planned scheduling min and max are within the window of BackgroundConstants.TIME_RANGE_MIN and BackgroundConstants.TIME_RANGE_MAX
     * earliest possible scheduling would be planned scheduling min
     * latest possible scheduling would be planned scheduling max
     *
     * Case 2: if planned scheduling min is before BackgroundConstants.TIME_RANGE_MIN
     * earliest possible scheduling would be BackgroundConstants.TIME_RANGE_MIN
     * latest possible scheduling would be BackgroundConstants.TIME_RANGE_MIN plus BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_MAX_DELAY
     *
     * Case 3: if planned scheduling min is after BackgroundConstants.TIME_RANGE_MAX
     * earliest possible scheduling would be planned scheduling min
     * latest possible scheduling would  BackgroundConstants.TIME_RANGE_MAX
     *
     * @param currentTime
     *
     * @return (earliestSchedulingTime, latestSchedulingTime)
     *
     */
    fun getPossibleSchedulingTimes(currentTime: DateTime): Pair<DateTime, DateTime> {
        val timeMin = currentTime.plusMinutes(BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_MIN_DELAY)
        val timeMax = timeMin.plusMinutes(BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_MAX_DELAY)

        val todayMidnight = DateTime().withTimeAtStartOfDay()
        val earliestAllowedScheduleTime =
            todayMidnight.plusMinutes(BackgroundConstants.TIME_RANGE_MIN)
        val latestAllowedScheduleTime =
            todayMidnight.plusMinutes(BackgroundConstants.TIME_RANGE_MAX)

        var earliestSchedulingTime = timeMin
        var latestSchedulingTime = timeMax

        if (timeMin.isAfter(earliestAllowedScheduleTime) && timeMax.isBefore(latestAllowedScheduleTime)) {
            earliestSchedulingTime = timeMin
            latestSchedulingTime = timeMax
        } else if (timeMin.isBefore(earliestAllowedScheduleTime)) {
            earliestSchedulingTime = earliestAllowedScheduleTime
            latestSchedulingTime =
                earliestAllowedScheduleTime.plusMinutes(BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_MAX_DELAY)
        } else if (timeMin.isAfter(earliestAllowedScheduleTime) && timeMax.isAfter(latestAllowedScheduleTime)) {
            earliestSchedulingTime = timeMin
            latestSchedulingTime = latestAllowedScheduleTime
        }
        return Pair(earliestSchedulingTime, latestSchedulingTime)
    }

    private fun convertTimestampToMinutesFromNow(input: DateTime, timeNow: DateTime): Int {
        val differenceInMilis = Duration(input.millis - timeNow.millis)
        return differenceInMilis.standardMinutes.toInt()
    }
}
