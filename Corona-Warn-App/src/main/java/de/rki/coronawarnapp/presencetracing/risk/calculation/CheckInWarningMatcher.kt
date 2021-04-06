package de.rki.coronawarnapp.presencetracing.risk.calculation

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningPackage
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.toOkioByteString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.joda.time.Instant
import timber.log.Timber
import java.lang.reflect.Modifier.PRIVATE
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class CheckInWarningMatcher @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun process(
        checkIns: List<CheckIn>,
        warningPackages: List<TraceWarningPackage>
    ): Result {
        val splitCheckIns = checkIns.flatMap { it.splitByMidnightUTC() }

        val matchLists: List<List<MatchesPerPackage>?> = createMatchingLaunchers(
            splitCheckIns,
            warningPackages,
            dispatcherProvider.IO
        ).awaitAll()

        val successful = if (matchLists.contains(null)) {
            Timber.e("Calculation partially failed.")
            false
        } else {
            Timber.d("Matching was successful.")
            true
        }

        return Result(
            successful = successful,
            processedPackages = matchLists.filterNotNull().flatten()
        )
    }

    data class Result(
        val successful: Boolean,
        val processedPackages: Collection<MatchesPerPackage> = emptyList(),
    )

    @VisibleForTesting(otherwise = PRIVATE)
    internal suspend fun createMatchingLaunchers(
        checkIns: List<CheckIn>,
        warningPackages: List<TraceWarningPackage>,
        coroutineContext: CoroutineContext
    ): List<Deferred<List<MatchesPerPackage>?>> {

        val launcher: CoroutineScope.(
            List<CheckIn>,
            List<TraceWarningPackage>
        ) -> Deferred<List<MatchesPerPackage>?> = { list, packageChunk ->
            async {
                try {
                    packageChunk.flatMap {
                        findMatches(list, it)
                        packageChunk.map { warningPackage ->
                            val overlaps = findMatches(list, warningPackage)
                            MatchesPerPackage(warningPackage = it, overlaps = overlaps)
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

    data class MatchesPerPackage(
        val warningPackage: TraceWarningPackage,
        val overlaps: List<CheckInWarningOverlap>,
    )
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
                            Timber.v("No match found for $checkIn and $warning")
                        } else {
                            Timber.i("Overlap was found $overlap")
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

    if (warning.locationIdHash.toOkioByteString() != traceLocationIdHash) return null

    Timber.i("traceLocationIdHash match for %s", traceLocationIdHash)

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
