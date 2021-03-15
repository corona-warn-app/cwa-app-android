package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.DownloadedCheckInsRepo
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CheckInMatcher @Inject constructor(
    private val checkInsRepository: CheckInRepository,
    private val downloadedCheckInsRepo: DownloadedCheckInsRepo
) {

    suspend fun execute(): List<CheckInOverlap> {
        val localCheckIns = checkInsRepository.allCheckIns.firstOrNull() ?: return emptyList()
        val downloadedPackages = downloadedCheckInsRepo.allCheckInsPackages.firstOrNull() ?: return emptyList()
        val relevantDownloadedCheckIns =
            downloadedPackages.flatMap {
                filterRelevantEventCheckIns(
                    localCheckIns,
                    it
                )
            }
        if (relevantDownloadedCheckIns.isEmpty()) return emptyList()

        //TODO split by midnight UTC

        // calculate time overlap
        val eventOverlapList = mutableListOf<CheckInOverlap>()
        relevantDownloadedCheckIns.forEach { relevantDownloadedCheckIn ->
            localCheckIns.forEach { localCheckIn ->
                val overlap = calculateOverlap(localCheckIn, relevantDownloadedCheckIn)
                if (overlap != null) eventOverlapList.add(overlap)
            }
        }
        return eventOverlapList
    }
}
