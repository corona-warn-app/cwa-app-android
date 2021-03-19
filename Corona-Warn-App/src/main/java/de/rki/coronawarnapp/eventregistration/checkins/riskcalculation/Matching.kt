package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate

suspend fun filterRelevantWarnings(
    checkIns: List<CheckIn>,
    traceTimeIntervalWarningPackage: TraceTimeIntervalWarningPackage
): List<TraceWarning.TraceTimeIntervalWarning> {
    val warnings = traceTimeIntervalWarningPackage.extractTraceTimeIntervalWarning()
    return warnings.filter { warning ->
        checkIns.find { checkIn ->
            warning.locationGuidHash == checkIn.traceLocationGuidHash
        } != null
    }
}

fun CheckIn.calculateOverlap(
    warning: TraceWarning.TraceTimeIntervalWarning
): CheckInOverlap? {

    if (traceLocationGuidHash !== warning.locationGuidHash) return null
    if (checkInEnd == null) return null

    val warningStartTimestamp = warning.startIntervalNumber.toTimestamp()
    val warningEndTimestamp = (warning.startIntervalNumber + warning.period).toTimestamp()

    val overlapStartTimestamp = kotlin.math.max(checkInStart.millis, warningStartTimestamp)
    val overlapEndTimestamp = kotlin.math.min(checkInEnd.millis, warningEndTimestamp)
    val overlapMillis = overlapEndTimestamp - overlapStartTimestamp

    if (overlapMillis <= 0) return null

    return CheckInOverlap(
        checkInId = id,
        localDate = Instant.ofEpochMilli(warningStartTimestamp).toLocalDate(),
        overlap = Duration(overlapMillis),
        transmissionRiskLevel = warning.transmissionRiskLevel
    )
}

fun Int.toTimestamp() = this * 600L * 1000L

data class CheckInOverlap(
    val checkInId: Long,
    val localDate: LocalDate,
    val overlap: Duration,
    val transmissionRiskLevel: Int
)
