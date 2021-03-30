package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class CheckInWarningMatcher @Inject constructor(
    private val checkInsRepository: CheckInRepository,
    private val traceTimeIntervalWarningRepository: TraceTimeIntervalWarningRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(): List<CheckInWarningOverlap> {
        val checkIns = checkInsRepository.allCheckIns.firstOrNull() ?: return emptyList()

        val warningPackages = traceTimeIntervalWarningRepository.allWarningPackages.firstOrNull() ?: return emptyList()

        val splitCheckIns = checkIns.flatMap { it.splitByMidnightUTC() }

        return createMatchingLaunchers(splitCheckIns, warningPackages, dispatcherProvider.IO)
            .awaitAll()
            .flatten()
    }
}

suspend fun createMatchingLaunchers(
    checkIns: List<CheckIn>,
    warningPackages: List<TraceTimeIntervalWarningPackage>,
    coroutineContext: CoroutineContext
): Collection<Deferred<List<CheckInWarningOverlap>>> {

    val launcher: CoroutineScope.(
        List<CheckIn>,
        List<TraceTimeIntervalWarningPackage>
    ) -> Deferred<List<CheckInWarningOverlap>> =
        { list, packageChunk ->
            async {
                packageChunk.flatMap {
                    findMatches(list, it)
                }
            }
        }

    // at most 4 parallel processes
    val chunkSize = (checkIns.size / 4) + 1

    return warningPackages.chunked(chunkSize).map { packageChunk ->
        withContext(context = coroutineContext) {
            launcher(checkIns, packageChunk)
        }
    }
}

suspend fun findMatches(
    checkIns: List<CheckIn>,
    warningPackage: TraceTimeIntervalWarningPackage
): List<CheckInWarningOverlap> {
    return warningPackage
        .extractTraceTimeIntervalWarnings()
        .flatMap { warning ->
            checkIns
                .mapNotNull { checkIn ->
                    checkIn.calculateOverlap(warning).also { overlap ->
                        if (overlap == null) {
                            Timber.d("No match/overlap found for $checkIn and $warning")
                        } else {
                            Timber.i("Overlap found $overlap")
                        }
                    }
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
