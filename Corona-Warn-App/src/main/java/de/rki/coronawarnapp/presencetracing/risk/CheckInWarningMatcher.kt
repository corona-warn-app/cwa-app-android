package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class CheckInWarningMatcher @Inject constructor(
    private val checkInsRepository: CheckInRepository,
    private val traceTimeIntervalWarningRepository: TraceTimeIntervalWarningRepository,
    private val presenceTracingRiskRepository: PresenceTracingRiskRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute() {

        val checkIns = checkInsRepository.allCheckIns.firstOrNull()
        val warningPackages = traceTimeIntervalWarningRepository.allWarningPackages.firstOrNull()

        if (checkIns.isNullOrEmpty() || warningPackages.isNullOrEmpty()) {
            presenceTracingRiskRepository.deleteAllMatches()
            Timber.i("No check-ins or packages available. Deleted all matches.")
            return
        }

        val splitCheckIns = checkIns.flatMap { it.splitByMidnightUTC() }

        val matches = createMatchingLaunchers(splitCheckIns, warningPackages, dispatcherProvider.IO)
            .awaitAll()
            .flatten()

        presenceTracingRiskRepository.replaceAllMatches(matches)
    }
}

suspend fun createMatchingLaunchers(
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

suspend fun findMatches(
    checkIns: List<CheckIn>,
    warningPackage: TraceTimeIntervalWarningPackage
): List<CheckInWarningOverlap> {
    return warningPackage
        .extractTraceTimeIntervalWarnings()
        .flatMap { warning ->
            checkIns
                .mapNotNull { checkIn ->
                    checkIn.calculateOverlap(warning, warningPackage.warningPackageId).also { overlap ->
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
    warning: TraceWarning.TraceTimeIntervalWarning,
    traceWarningPackageId: Long
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
        transmissionRiskLevel = warning.transmissionRiskLevel,
        traceWarningPackageId = traceWarningPackageId,
        startTime = Instant.ofEpochMilli(overlapStartTimestamp),
        endTime = Instant.ofEpochMilli(overlapEndTimestamp)
    )
}
