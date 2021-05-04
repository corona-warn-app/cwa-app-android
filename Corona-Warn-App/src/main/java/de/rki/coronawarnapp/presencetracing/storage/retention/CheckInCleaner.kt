package de.rki.coronawarnapp.presencetracing.storage.retention

import dagger.Reusable
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.isOutOfRetention
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CheckInCleaner @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val timeStamper: TimeStamper
) {

    suspend fun cleanUp() {
        Timber.d("Starting to clean up stale check-ins.")

        val now = timeStamper.nowUTC
        val checkInsToDelete = checkInRepository.allCheckIns.first()
            .filter { checkIn -> checkIn.isOutOfRetention(now) }

        Timber.d("Cleaning up ${checkInsToDelete.size} stale check-ins.")

        checkInRepository.deleteCheckIns(checkInsToDelete)

        Timber.d("Clean up of stale check-ins completed.")
    }
}
