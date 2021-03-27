package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import org.joda.time.Instant

internal suspend fun findMatches(
    checkIns: List<CheckIn>,
    warningPackage: TraceTimeIntervalWarningPackage
): List<CheckInWarningOverlap> {

    val relevantWarnings =
        filterRelevantWarnings(
            checkIns,
            warningPackage
        )

    if (relevantWarnings.isEmpty()) return emptyList()

    return relevantWarnings
        .flatMap { warning ->
            checkIns
                .flatMap { it.splitByMidnightUTC() }
                .mapNotNull { it.calculateOverlap(warning, warningPackage.id) }
        }
}

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
): CheckInWarningOverlap?  {

    if (warning.locationGuidHash != locationGuidHash) return null

    val warningStartTimestamp = warning.startIntervalNumber.tenMinIntervalToMillis()
    val warningEndTimestamp = (warning.startIntervalNumber + warning.period).tenMinIntervalToMillis()

    val overlapStartTimestamp = kotlin.math.max(checkInStart.millis, warningStartTimestamp)
    val overlapEndTimestamp = kotlin.math.min(checkInEnd.millis, warningEndTimestamp)
    val overlapMillis = overlapEndTimestamp - overlapStartTimestamp

    if (overlapMillis <= 0) return null

    return CheckInWarningOverlap(
        checkInId = id,
        transmissionRiskLevel = warning.transmissionRiskLevel,
        traceWarningPackageId = traceWarningPackageId,
        startTime = Instant.ofEpochMilli(overlapStartTimestamp),
        endTime = Instant.ofEpochMilli(overlapEndTimestamp)
    )
}
