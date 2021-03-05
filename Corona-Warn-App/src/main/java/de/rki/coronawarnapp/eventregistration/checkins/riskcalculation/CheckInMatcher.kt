package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.eventregistration.checkins.CheckInsRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.DownloadedCheckInsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckInMatcher @Inject constructor(
    private val checkInsRepository: CheckInsRepository,
    private val downloadedCheckInsRepo: DownloadedCheckInsRepo
) {

    val eventOverlapFlow = MutableStateFlow<List<EventOverlap>>(emptyList())

    suspend fun execute() {
        val localCheckIns = checkInsRepository.allCheckIns.first()
        val relevantCheckIns = downloadedCheckInsRepo
            .allCheckInsPackages
            .first()
            .flatMap {
                filterRelevantEventCheckIns(
                    localCheckIns,
                    it
                )
            }

        val eventOverlapList = mutableListOf<EventOverlap>()
        relevantCheckIns.forEach { reported ->
            localCheckIns.forEach { local ->
                val overlap = calculateOverlap(local, reported)
                if (overlap != null) eventOverlapList.add(overlap)
            }
        }
        eventOverlapFlow.value = eventOverlapList
    }
}
