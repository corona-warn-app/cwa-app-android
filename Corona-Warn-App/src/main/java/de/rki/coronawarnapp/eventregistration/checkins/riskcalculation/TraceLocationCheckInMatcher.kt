package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class TraceLocationCheckInMatcher @Inject constructor(
    private val checkInsRepository: CheckInRepository,
    private val traceTimeIntervalWarningRepository: TraceTimeIntervalWarningRepository,
    private val presenceTracingRiskRepository: PresenceTracingRiskRepository
) {
    suspend fun execute() {

        val checkIns = checkInsRepository.allCheckIns.firstOrNull()
        val warningPackages = traceTimeIntervalWarningRepository.allWarningPackages.firstOrNull()

        if (checkIns.isNullOrEmpty() || warningPackages.isNullOrEmpty()) {
            presenceTracingRiskRepository.deleteAllMatches()
            return
        }

        val matches = warningPackages
            .flatMap { warningPackage ->
                findMatches(checkIns, warningPackage)
            }

        presenceTracingRiskRepository.replaceAllMatches(matches)
    }
}
