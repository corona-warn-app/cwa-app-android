package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.download.CheckInsPackage
import org.joda.time.Duration
import org.joda.time.Instant

suspend fun filterRelevantEventCheckIns(
    localCheckIns: List<CheckIn>,
    checkInsPackage: CheckInsPackage
): List<CheckIn> {
    val reportedCheckIns = checkInsPackage.extractCheckIns()
    return reportedCheckIns.filter { reported ->
        localCheckIns.find { local ->
            reported.guid == local.guid
        } != null
    }
}

fun calculateOverlap(
    local: CheckIn,
    reported: CheckIn
): CheckInOverlap? {
    if (local.guid != reported.guid) return null
    // TODO implement calculation
    return null
}

data class CheckInOverlap(
    val checkInGuid: String,
    val overlap: Duration
)

fun min(first: Instant, second: Instant) = Instant(kotlin.math.min(first.millis, second.millis))

fun max(first: Instant, second: Instant) = Instant(kotlin.math.max(first.millis, second.millis))
