package de.rki.coronawarnapp.eventregistration.storage.retention

import dagger.Reusable
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class CheckInCleaner @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val timeStamper: TimeStamper
) {

    suspend fun cleanUp() {
        Timber.d("Starting to clean up stale check-ins.")
        val retentionThreshold = (timeStamper.nowUTC.seconds - RETENTION_SECONDS)
        val checkInsToDelete = checkInRepository.allCheckIns.first()
            .filter {
                it.checkInEnd.seconds < retentionThreshold
            }
        Timber.d("Cleaning up ${checkInsToDelete.size} stale check-ins.")
        checkInRepository.deleteCheckIns(checkInsToDelete)
        Timber.d("Clean up of stale check-ins completed.")
    }

    companion object {
        private const val RETENTION_DAYS = 15
        private val RETENTION_SECONDS = TimeUnit.DAYS.toSeconds(RETENTION_DAYS.toLong())
    }
}
