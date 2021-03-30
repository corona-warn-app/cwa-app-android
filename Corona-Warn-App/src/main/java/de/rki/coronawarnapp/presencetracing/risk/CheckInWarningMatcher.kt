package de.rki.coronawarnapp.presencetracing.risk

import androidx.annotation.VisibleForTesting
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
import java.lang.reflect.Modifier.PRIVATE
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class CheckInWarningMatcher @Inject constructor(
    private val checkInsRepository: CheckInRepository,
    private val traceTimeIntervalWarningRepository: TraceTimeIntervalWarningRepository,
    private val presenceTracingRiskRepository: PresenceTracingRiskRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(): List<CheckInWarningOverlap> {

        val checkIns = checkInsRepository.allCheckIns.firstOrNull()
        val warningPackages = traceTimeIntervalWarningRepository.allWarningPackages.firstOrNull()

        if (checkIns.isNullOrEmpty() || warningPackages.isNullOrEmpty()) {
            Timber.i("No check-ins or packages available. Deleting all matches.")
            presenceTracingRiskRepository.deleteAllMatches()
            return emptyList()
        }

        val splitCheckIns = checkIns.flatMap { it.splitByMidnightUTC() }

        val matchLists = createMatchingLaunchers(splitCheckIns, warningPackages, dispatcherProvider.IO)
            .awaitAll()

        if (matchLists.contains(null)) {
            Timber.e("Error occurred during matching. Deleting all stale matches.")
            presenceTracingRiskRepository.deleteAllMatches()
            //TODO report calculation failed to show on home card
            return emptyList()
        }

        val matches = matchLists.filterNotNull().flatten()
        presenceTracingRiskRepository.replaceAllMatches(matches)
        return matches
    }
}

@VisibleForTesting(otherwise = PRIVATE)
internal suspend fun createMatchingLaunchers(
    checkIns: List<CheckIn>,
    warningPackages: List<TraceTimeIntervalWarningPackage>,
    coroutineContext: CoroutineContext
): Collection<Deferred<List<CheckInWarningOverlap>?>> {

    val launcher: CoroutineScope.(
        List<CheckIn>,
        List<TraceTimeIntervalWarningPackage>
    ) -> Deferred<List<CheckInWarningOverlap>?> =
        { list, packageChunk ->
            async {
                try {

                    packageChunk.flatMap {
                        findMatches(list, it)
                    }
                } catch (e: Throwable) {
                    Timber.e("Failed to process packages $packageChunk")
                    null
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

@VisibleForTesting(otherwise = PRIVATE)
internal suspend fun findMatches(
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

@VisibleForTesting(otherwise = PRIVATE)
internal fun CheckIn.calculateOverlap(
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
