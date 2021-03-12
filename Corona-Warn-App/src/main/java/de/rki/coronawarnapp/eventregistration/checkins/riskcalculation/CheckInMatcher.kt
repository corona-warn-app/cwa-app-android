package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.DownloadedCheckInsRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CheckInMatcher @Inject constructor(
    private val checkInsRepository: CheckInRepository,
    private val downloadedCheckInsRepo: DownloadedCheckInsRepo
) {

    val checkInOverlapFlow = MutableStateFlow<List<CheckInOverlap>>(emptyList())

    suspend fun execute() {
        val localCheckIns = checkInsRepository.allCheckIns.firstOrNull() ?: return
        val downloadedPackages = downloadedCheckInsRepo.allCheckInsPackages.firstOrNull() ?: return
        val relevantDownloadedCheckIns =
            downloadedPackages.flatMap {
                filterRelevantEventCheckIns(
                    localCheckIns,
                    it
                )
            }
        if (relevantDownloadedCheckIns.isEmpty()) return

        val eventOverlapList = mutableListOf<CheckInOverlap>()
        relevantDownloadedCheckIns.forEach { relevantDownloadedCheckIn ->
            localCheckIns.forEach { localCheckIn ->
                val overlap = calculateOverlap(localCheckIn, relevantDownloadedCheckIn)
                if (overlap != null) eventOverlapList.add(overlap)
            }
        }
        checkInOverlapFlow.value = eventOverlapList
    }
}
