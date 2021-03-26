package de.rki.coronawarnapp.presencetracing.warning.riskcalculation

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import org.joda.time.Duration
import org.joda.time.Instant

suspend fun filterRelevantWarnings(
    checkIns: List<CheckIn>,
    traceTimeIntervalWarningPackage: TraceTimeIntervalWarningPackage
): List<TraceWarning.TraceTimeIntervalWarning> {
    val warnings = traceTimeIntervalWarningPackage.extractTraceTimeIntervalWarning()
    return warnings.filter { warning ->
        checkIns.find { checkIn ->
            warning.locationGuidHash == checkIn.locationGuidHash
        } != null
    }
}

fun CheckIn.calculateOverlap(
    warning: TraceWarning.TraceTimeIntervalWarning
): CheckInOverlap? {

    if (warning.locationGuidHash != locationGuidHash) return null

    val warningStartTimestamp = warning.startIntervalNumber.tenMinIntervalToMillis()
    val warningEndTimestamp = (warning.startIntervalNumber + warning.period).tenMinIntervalToMillis()

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

// converts number of 10min intervals into milliseconds
private fun Int.tenMinIntervalToMillis() = this * 600L * 1000L
