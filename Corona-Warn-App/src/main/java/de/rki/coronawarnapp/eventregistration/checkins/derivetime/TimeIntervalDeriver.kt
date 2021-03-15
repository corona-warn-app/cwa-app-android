package de.rki.coronawarnapp.eventregistration.checkins.derivetime

import de.rki.coronawarnapp.appconfig.PresenceTracingSubmissionParamContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.Range
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToLong

private val INTERVAL_LENGTH_IN_SECONDS = TimeUnit.MINUTES.toSeconds(10L)

private fun alignToInterval(timestamp: Long) =
    (timestamp / INTERVAL_LENGTH_IN_SECONDS) * INTERVAL_LENGTH_IN_SECONDS

private fun Range.inRange(value: Long): Boolean {
    val inRage = when {
        minExclusive && value <= min -> false
        !minExclusive && value < min -> false
        maxExclusive && value >= max -> false
        !maxExclusive && value > max -> false
        else -> true
    }

    Timber.d(
        "Value:%s isInRange:%s - Range{min:%s,max:%s,minExclusive:%s,maxExclusive:%s}",
        value,
        inRage,
        min,
        max,
        minExclusive,
        maxExclusive
    )
    return inRage
}

fun PresenceTracingSubmissionParamContainer.deriveTime(
    startTimestamp: Long,
    endTimestamp: Long
): Pair<Long, Long>? {
    val durationInSeconds = max(0, endTimestamp - startTimestamp)
    Timber.d("durationInSeconds: $durationInSeconds")

    val durationInMinutes = TimeUnit.SECONDS.toMinutes(durationInSeconds)
    Timber.d("durationInMinutes: $durationInMinutes")

    val dropDueToDuration = durationFilters.any { durationFilter ->
        durationFilter.dropIfMinutesInRange.inRange(durationInMinutes)
    }
    Timber.d("dropDueToDuration: $dropDueToDuration")
    if (dropDueToDuration) return null

    val aerosoleDecays = aerosoleDecayLinearFunctions.filter { aerosole ->
        aerosole.minutesRange.inRange(durationInMinutes)
    }.map { aerosole ->
        aerosole.slope * durationInSeconds + TimeUnit.MINUTES.toSeconds(aerosole.intercept.toLong())
    }
    Timber.d("aerosoleDecays:$aerosoleDecays")
    val aerosoleDecayInSeconds = aerosoleDecays.firstOrNull() ?: 0.0 // Default: zero, i.e. 'no decay'
    Timber.d("aerosoleDecayInSeconds: $aerosoleDecayInSeconds")

    val relevantEndTimestamp = endTimestamp + aerosoleDecayInSeconds.toLong()
    val relevantStartIntervalTimestamp = alignToInterval(startTimestamp)
    val relevantEndIntervalTimestamp = alignToInterval(relevantEndTimestamp)
    val overlapWithStartInterval = relevantStartIntervalTimestamp + INTERVAL_LENGTH_IN_SECONDS - startTimestamp
    val overlapWithEndInterval = relevantEndTimestamp - relevantEndIntervalTimestamp
    Timber.d("overlapWithStartInterval: $overlapWithStartInterval")
    Timber.d("overlapWithEndInterval: $overlapWithEndInterval")

    val targetDurationInSeconds =
        ((durationInSeconds + aerosoleDecayInSeconds) / INTERVAL_LENGTH_IN_SECONDS).roundToLong() * INTERVAL_LENGTH_IN_SECONDS
    Timber.d("targetDurationInSeconds:$targetDurationInSeconds")

    return if (overlapWithEndInterval > overlapWithStartInterval) {
        Timber.d("overlapWithEndInterval: $overlapWithEndInterval > overlapWithStartInterval: $overlapWithStartInterval")
        val newEndTimestamp = relevantEndIntervalTimestamp + INTERVAL_LENGTH_IN_SECONDS
        val newStartTimestamp = newEndTimestamp - targetDurationInSeconds
        newStartTimestamp to newEndTimestamp
    } else {
        Timber.d("overlapWithEndInterval: $overlapWithEndInterval > overlapWithStartInterval: $overlapWithStartInterval")
        val newEndTimestamp = relevantStartIntervalTimestamp + targetDurationInSeconds
        relevantStartIntervalTimestamp to newEndTimestamp
    }
}
