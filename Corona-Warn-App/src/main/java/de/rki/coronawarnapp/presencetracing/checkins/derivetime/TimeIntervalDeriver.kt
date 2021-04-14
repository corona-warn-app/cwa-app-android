package de.rki.coronawarnapp.presencetracing.checkins.derivetime

import de.rki.coronawarnapp.appconfig.PresenceTracingSubmissionParamContainer
import de.rki.coronawarnapp.risk.DefaultRiskLevels.Companion.inRange
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToLong

private val INTERVAL_LENGTH_IN_SECONDS = TimeUnit.MINUTES.toSeconds(10L)

private fun alignToInterval(timestamp: Long) =
    (timestamp / INTERVAL_LENGTH_IN_SECONDS) * INTERVAL_LENGTH_IN_SECONDS

/**
 * Derive CheckIn start and end times
 * @param startTimestampInSeconds [Long] timestamp in seconds
 * @param endTimestampInSeconds [Long] timestamp in seconds
 *
 * @return [DerivedTimes] holds derived startTime and endTime in seconds
 */
fun PresenceTracingSubmissionParamContainer.deriveTime(
    startTimestampInSeconds: Long,
    endTimestampInSeconds: Long
): DerivedTimes? {
    Timber.d("Starting deriveTime ...")
    val durationInSeconds = max(0, endTimestampInSeconds - startTimestampInSeconds)
    Timber.d("durationInSeconds: $durationInSeconds")

    val durationInMinutes = TimeUnit.SECONDS.toMinutes(durationInSeconds)
    Timber.d("durationInMinutes: $durationInMinutes")

    val dropDueToDuration: Boolean = durationFilters.any { durationFilter ->
        durationFilter.dropIfMinutesInRange.inRange(durationInMinutes)
    }
    Timber.d("dropDueToDuration: $dropDueToDuration")
    if (dropDueToDuration) return null

    val aerosoleDecays: List<Double> = aerosoleDecayLinearFunctions.filter { aerosole ->
        aerosole.minutesRange.inRange(durationInMinutes)
    }.map { aerosole ->
        aerosole.slope * durationInSeconds + TimeUnit.MINUTES.toSeconds(aerosole.intercept.toLong())
    }
    Timber.d("aerosoleDecays:$aerosoleDecays")
    val aerosoleDecayInSeconds: Double = aerosoleDecays.firstOrNull() ?: 0.0 // Default: zero, i.e. 'no decay'
    Timber.d("aerosoleDecayInSeconds: $aerosoleDecayInSeconds")

    val relevantEndTimestamp = endTimestampInSeconds + aerosoleDecayInSeconds.toLong()
    val relevantStartIntervalTimestamp = alignToInterval(startTimestampInSeconds)
    val relevantEndIntervalTimestamp = alignToInterval(relevantEndTimestamp)
    val overlapWithStartInterval = relevantStartIntervalTimestamp + INTERVAL_LENGTH_IN_SECONDS - startTimestampInSeconds
    val overlapWithEndInterval = relevantEndTimestamp - relevantEndIntervalTimestamp
    Timber.d("overlapWithStartInterval: $overlapWithStartInterval")
    Timber.d("overlapWithEndInterval: $overlapWithEndInterval")

    val targetDurationInSeconds =
        ((durationInSeconds + aerosoleDecayInSeconds) / INTERVAL_LENGTH_IN_SECONDS).roundToLong() *
            INTERVAL_LENGTH_IN_SECONDS

    Timber.d("targetDurationInSeconds:$targetDurationInSeconds")

    return if (overlapWithEndInterval > overlapWithStartInterval) {
        Timber.d(
            "overlapWithEndInterval:%s > overlapWithStartInterval:%s",
            overlapWithEndInterval,
            overlapWithStartInterval
        )
        val newEndTimestamp = relevantEndIntervalTimestamp + INTERVAL_LENGTH_IN_SECONDS
        val newStartTimestamp = newEndTimestamp - targetDurationInSeconds
        DerivedTimes(
            startTimeSeconds = newStartTimestamp,
            endTimeSeconds = newEndTimestamp
        )
    } else {
        Timber.d(
            "overlapWithEndInterval:%s, overlapWithStartInterval:%s",
            overlapWithEndInterval,
            overlapWithStartInterval
        )
        val newEndTimestamp = relevantStartIntervalTimestamp + targetDurationInSeconds
        DerivedTimes(
            startTimeSeconds = relevantStartIntervalTimestamp,
            endTimeSeconds = newEndTimestamp
        )
    }
}

/**
 * Represents output of [deriveTime]
 */
data class DerivedTimes(
    val startTimeSeconds: Long,
    val endTimeSeconds: Long
)
