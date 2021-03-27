package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CheckInWarningMatcher @Inject constructor(
    private val checkInsRepository: CheckInRepository,
    private val traceTimeIntervalWarningRepository: TraceTimeIntervalWarningRepository
) {
    suspend fun execute(): List<CheckInWarningOverlap> {
        val checkIns = checkInsRepository.allCheckIns.firstOrNull() ?: return emptyList()

        val warningPackages = traceTimeIntervalWarningRepository.allWarningPackages.firstOrNull() ?: return emptyList()

        return warningPackages
            .flatMap { warningPackage ->
                findMatches(checkIns, warningPackage)
            }
    }
}

private suspend fun findMatches(
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
                .mapNotNull { it.calculateOverlap(warning) }
        }
}
