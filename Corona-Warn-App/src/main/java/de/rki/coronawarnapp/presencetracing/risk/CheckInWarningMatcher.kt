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
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun execute(): List<CheckInWarningOverlap> {
        val checkIns = checkInsRepository.allCheckIns.firstOrNull() ?: return emptyList()

        val warningPackages = traceTimeIntervalWarningRepository.allWarningPackages.firstOrNull() ?: return emptyList()

        val splitCheckIns = checkIns.flatMap { it.splitByMidnightUTC() }

        return launchMatching(splitCheckIns, warningPackages, dispatcherProvider.IO)
            .awaitAll()
            .flatten()
    }
}
