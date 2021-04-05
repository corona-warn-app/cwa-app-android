package de.rki.coronawarnapp.presencetracing.risk

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningPackage
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
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
    private val traceWarningRepository: TraceWarningRepository,
    private val presenceTracingRiskRepository: PresenceTracingRiskRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(): List<CheckInWarningOverlap> {

        presenceTracingRiskRepository.deleteStaleData()

        val warningPackages = traceWarningRepository.unprocessedWarningPackages.firstOrNull()

        val checkIns = checkInsRepository.allCheckIns.firstOrNull()
        if (checkIns.isNullOrEmpty()) {
            Timber.i("No check-ins available. Deleting all matches.")
            presenceTracingRiskRepository.deleteAllMatches()
            presenceTracingRiskRepository.reportSuccessfulCalculation(
                warningPackages = warningPackages,
                overlapList = emptyList()
            )
            return emptyList()
        }

        if (warningPackages.isNullOrEmpty()) {
            // nothing to be done here
            Timber.i("No new warning packages available.")
            presenceTracingRiskRepository.reportSuccessfulCalculation(
                warningPackages = warningPackages,
                overlapList = emptyList()
            )
            return emptyList()
        }

        val splitCheckIns = checkIns.flatMap { it.splitByMidnightUTC() }

        val matchLists = createMatchingLaunchers(
            splitCheckIns,
            warningPackages,
            dispatcherProvider.IO
        ).awaitAll()

        if (matchLists.contains(null)) {
            Timber.e("Error occurred during matching. Abort calculation.")
            presenceTracingRiskRepository.reportFailedCalculation()
            return emptyList()
        }

        val matches = matchLists.filterNotNull().flatten()

        presenceTracingRiskRepository.reportSuccessfulCalculation(warningPackages, matches)

        warningPackages.forEach {
            traceWarningRepository.markPackageProcessed(it.packageId)
        }

        return matches
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal suspend fun createMatchingLaunchers(
        checkIns: List<CheckIn>,
        warningPackages: List<TraceWarningPackage>,
        coroutineContext: CoroutineContext
    ): Collection<Deferred<List<CheckInWarningOverlap>?>> {

        val launcher: CoroutineScope.(
            List<CheckIn>,
            List<TraceWarningPackage>
        ) -> Deferred<List<CheckInWarningOverlap>?> =
            { list, packageChunk ->
                async {
                    try {
                        packageChunk.flatMap {
                            findMatches(list, it)
                            packageChunk.flatMap { warningPackage ->
                                findMatches(list, warningPackage)
                            }
                        }
                    } catch (e: Throwable) {
                        Timber.e(e, "Failed to process packages $packageChunk")
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
}

@VisibleForTesting(otherwise = PRIVATE)
internal suspend fun findMatches(
    checkIns: List<CheckIn>,
    warningPackage: TraceWarningPackage
): List<CheckInWarningOverlap> {
    return warningPackage
        .extractWarnings()
        .flatMap { warning ->
            checkIns
                .mapNotNull { checkIn ->
                    checkIn.calculateOverlap(warning, warningPackage.packageId).also { overlap ->
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

    // TODO this is not correct anymore
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
