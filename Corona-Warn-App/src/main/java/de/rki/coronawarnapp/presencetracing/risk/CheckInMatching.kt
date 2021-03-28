package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.joda.time.Duration
import org.joda.time.Instant
import kotlin.coroutines.CoroutineContext

suspend fun launchMatching(
    checkIns: List<CheckIn>,
    warningPackages: List<TraceTimeIntervalWarningPackage>,
    coroutineContext: CoroutineContext
): Collection<Deferred<List<CheckInWarningOverlap>>> {
    val launcher: CoroutineScope.(
        List<CheckIn>,
        TraceTimeIntervalWarningPackage
    ) -> Deferred<List<CheckInWarningOverlap>> =
        { list, warningPackage ->
            async {
                findMatches(list, warningPackage)
            }
        }

    return warningPackages.map { warningPackage ->
        withContext(context = coroutineContext) {
            launcher(checkIns, warningPackage)
        }
    }
}

internal suspend fun findMatches(
    checkIns: List<CheckIn>,
    warningPackage: TraceTimeIntervalWarningPackage
): List<CheckInWarningOverlap> {
    return warningPackage
        .extractTraceTimeIntervalWarnings()
        .flatMap { warning ->
            checkIns
                .mapNotNull {
                    it.calculateOverlap(warning)
                }
        }
}

fun CheckIn.calculateOverlap(
    warning: TraceWarning.TraceTimeIntervalWarning
): CheckInWarningOverlap? {

    if (warning.locationGuidHash != locationGuidHash) return null

    val warningStartTimestamp = warning.startIntervalNumber.tenMinIntervalToMillis()
    val warningEndTimestamp = (warning.startIntervalNumber + warning.period).tenMinIntervalToMillis()

    val overlapStartTimestamp = kotlin.math.max(checkInStart.millis, warningStartTimestamp)
    val overlapEndTimestamp = kotlin.math.min(checkInEnd.millis, warningEndTimestamp)
    val overlapMillis = overlapEndTimestamp - overlapStartTimestamp

    if (overlapMillis <= 0) return null

    return CheckInWarningOverlap(
        checkInId = id,
        localDate = Instant.ofEpochMilli(warningStartTimestamp).toLocalDate(),
        overlap = Duration(overlapMillis),
        transmissionRiskLevel = warning.transmissionRiskLevel
    )
}

// converts number of 10min intervals into milliseconds
private fun Int.tenMinIntervalToMillis() = this * 600L * 1000L
