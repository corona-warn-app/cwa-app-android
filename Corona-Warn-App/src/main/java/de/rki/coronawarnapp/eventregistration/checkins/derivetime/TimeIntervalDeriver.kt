package de.rki.coronawarnapp.eventregistration.checkins.derivetime

import de.rki.coronawarnapp.appconfig.PresenceTracingSubmissionParamContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.Range
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToLong

private const val MINUTES_INTERVAL = 10L

private fun minutesInSeconds(minutes: Long) = TimeUnit.MINUTES.toSeconds(minutes)

private fun alignToInterval(timestamp: Long): Long {
    val minutesInSeconds = minutesInSeconds(MINUTES_INTERVAL)
    return (timestamp / minutesInSeconds) * minutesInSeconds
}

private fun Range.inRange(value: Long): Boolean {
    if (minExclusive && value <= min) return false
    else if (!minExclusive && value < min) return false

    if (maxExclusive && value >= max) return false
    else if (!maxExclusive && value > max) return false

    return true
}

fun PresenceTracingSubmissionParamContainer.deriveTime(
    startTimestamp: Long,
    endTimestamp: Long
): Pair<Long, Long>? {
    val durationInSeconds = max(0, endTimestamp - startTimestamp)
    val durationInMinutes = TimeUnit.SECONDS.toMinutes(durationInSeconds)
    Timber.d("durationInSeconds: $durationInSeconds")

    val dropDueToDuration = durationFilters.any { durationFilter ->
        durationFilter.dropIfMinutesInRange.inRange(durationInMinutes)
    }

    Timber.d("dropDueToDuration: $dropDueToDuration")
    if (dropDueToDuration) return null

    val aerosoleDecayInSeconds = aerosoleDecayLinearFunctions.filter { aerosole ->
        aerosole.minutesRange.inRange(durationInMinutes)
    }.map { aerosole ->
        aerosole.slope * durationInSeconds + minutesInSeconds(aerosole.intercept.toLong())
    }.firstOrNull() ?: 0.0 //default: zero, i.e. 'no decay'

    val relevantEndTimestamp = endTimestamp + aerosoleDecayInSeconds
    val relevantStartIntervalTimestamp = alignToInterval(startTimestamp)
    val relevantEndIntervalTimestamp = alignToInterval(relevantEndTimestamp.toLong())

    val overlapWithStartInterval = relevantStartIntervalTimestamp + MINUTES_INTERVAL - startTimestamp
    val overlapWithEndInterval = relevantEndTimestamp - relevantEndIntervalTimestamp
    Timber.d("overlapWithStartInterval: $overlapWithStartInterval")
    Timber.d("overlapWithEndInterval: $overlapWithEndInterval")

    val targetDurationInSeconds =
        ((durationInSeconds + aerosoleDecayInSeconds) / MINUTES_INTERVAL).roundToLong() * MINUTES_INTERVAL
    Timber.d("targetDurationInSeconds: $targetDurationInSeconds")

    return if (overlapWithEndInterval > overlapWithStartInterval) {
        val newEndTimestamp = relevantEndIntervalTimestamp + MINUTES_INTERVAL
        val newStartTimestamp = newEndTimestamp - targetDurationInSeconds
        newStartTimestamp to newEndTimestamp
    } else {
        val newEndTimestamp = relevantStartIntervalTimestamp + targetDurationInSeconds
        relevantStartIntervalTimestamp to newEndTimestamp
    }
}
