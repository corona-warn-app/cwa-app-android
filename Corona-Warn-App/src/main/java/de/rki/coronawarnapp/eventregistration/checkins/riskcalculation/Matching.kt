package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.eventregistration.checkins.EventCheckIn
import org.joda.time.Duration
import org.joda.time.Instant

fun filterRelevantEventCheckIns(
    localCheckIns: List<EventCheckIn>,
    reportedCheckIns: List<EventCheckIn>
): List<EventCheckIn> {
    return reportedCheckIns.filter { reported ->
        localCheckIns.find { local ->
            reported.guid == local.guid
        } != null
    }
}

fun calculateOverlap(
    local: EventCheckIn,
    reported: EventCheckIn
): EventOverlap? {
    if (local.guid != reported.guid) return null
    val start = max(local.startTime, reported.startTime)
    val end = min(local.endTime, reported.endTime)
    return if (start < end) EventOverlap(local.guid, Duration(start, end))
    else null
}

data class EventOverlap(
    val guid: String,
    val overlap: Duration
)

fun min(first: Instant, second: Instant) = Instant(kotlin.math.min(first.millis, second.millis))

fun max(first: Instant, second: Instant) = Instant(kotlin.math.max(first.millis, second.millis))
