package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class TraceLocationCheckInMatcher @Inject constructor(
    private val checkInsRepository: CheckInRepository,
    private val traceTimeIntervalWarningRepository: TraceTimeIntervalWarningRepository
) {
    suspend fun execute(): List<CheckInOverlap> {
        val checkIns = checkInsRepository.allCheckIns.firstOrNull() ?: return emptyList()
        val warningPackages = traceTimeIntervalWarningRepository.allWarningPackages.firstOrNull() ?: return emptyList()

        val relevantWarnings = warningPackages
            .flatMap { warningPackage ->
                filterRelevantWarnings(
                    checkIns,
                    warningPackage
                )
            }

        if (relevantWarnings.isEmpty()) return emptyList()

        return relevantWarnings
            .flatMap { warning ->
                checkIns
                    .flatMap { it.splitByMidnightUTC() }
                    .mapNotNull { it.calculateOverlap(warning) }
            }
    }
}
