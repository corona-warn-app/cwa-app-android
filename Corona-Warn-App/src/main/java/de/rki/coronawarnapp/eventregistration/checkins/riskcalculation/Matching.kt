package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
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
    warning: TraceWarning.TraceTimeIntervalWarning,
    traceWarningPackageId: Long
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
        transmissionRiskLevel = warning.transmissionRiskLevel,
        traceWarningPackageId = traceWarningPackageId,
        startTime = Instant.ofEpochMilli(overlapStartTimestamp),
        endTime = Instant.ofEpochMilli(overlapEndTimestamp)

    )
}
