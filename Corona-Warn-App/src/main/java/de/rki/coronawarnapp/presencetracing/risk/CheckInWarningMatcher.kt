package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

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
            return
        }

        val splitCheckIns = checkIns.flatMap { it.splitByMidnightUTC() }

        val matches = createMatchingLaunchers(splitCheckIns, warningPackages, dispatcherProvider.IO)
            .awaitAll()
            .flatten()

        presenceTracingRiskRepository.replaceAllMatches(matches)
    }
}
