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
import okio.ByteString.Companion.toByteString
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

        presenceTracingRiskRepository.deleteStaleData()

        val checkIns = checkInsRepository.allCheckIns.firstOrNull()
        if (checkIns.isNullOrEmpty()) {
            Timber.i("No check-ins available. Deleting all matches.")
            presenceTracingRiskRepository.deleteAllMatches()
            presenceTracingRiskRepository.reportSuccessfulCalculation(emptyList())
            return emptyList()
        }

        val warningPackages = traceTimeIntervalWarningRepository.allWarningPackages.firstOrNull()

        if (warningPackages.isNullOrEmpty()) {
            // nothing to be done here
            return emptyList()
        }

        val splitCheckIns = checkIns.flatMap { it.splitByMidnightUTC() }

        val matchLists = createMatchingLaunchers(
            splitCheckIns,
            warningPackages,
            dispatcherProvider.IO
        )
            .awaitAll()

        if (matchLists.contains(null)) {
            Timber.e("Error occurred during matching. Abort calculation.")
            presenceTracingRiskRepository.reportFailedCalculation()
            return emptyList()
        }

        // delete stale matches from new packages and mark packages as processed
        warningPackages.forEach {
            presenceTracingRiskRepository.deleteMatchesOfPackage(it.warningPackageId)
            presenceTracingRiskRepository.markPackageProcessed(it.warningPackageId)
        }
        val matches = matchLists.filterNotNull().flatten()

        presenceTracingRiskRepository.reportSuccessfulCalculation(matches)

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
    traceWarningPackageId: String
): CheckInWarningOverlap? {

    if (warning.locationIdHash.toByteArray().toByteString() != traceLocationIdHash) return null

    val warningStartMillis = warning.startIntervalNumber.tenMinIntervalToMillis()
    val warningEndMillis = (warning.startIntervalNumber + warning.period).tenMinIntervalToMillis()

    val overlapStartMillis = kotlin.math.max(checkInStart.millis, warningStartMillis)
    val overlapEndMillis = kotlin.math.min(checkInEnd.millis, warningEndMillis)
    val overlapMillis = overlapEndMillis - overlapStartMillis

    if (overlapMillis <= 0) return null

    return CheckInWarningOverlap(
        checkInId = id,
        transmissionRiskLevel = warning.transmissionRiskLevel,
        traceWarningPackageId = traceWarningPackageId,
        startTime = Instant.ofEpochMilli(overlapStartMillis),
        endTime = Instant.ofEpochMilli(overlapEndMillis)
    )
}
