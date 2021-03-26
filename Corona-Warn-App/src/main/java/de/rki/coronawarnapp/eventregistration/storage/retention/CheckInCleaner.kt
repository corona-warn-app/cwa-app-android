package de.rki.coronawarnapp.eventregistration.storage.retention

import dagger.Reusable
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeConstants
import javax.inject.Inject

@Reusable
class CheckInCleaner @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val timeStamper: TimeStamper
) {

    suspend fun cleanUp() {
        val retentionThreshold = (timeStamper.nowUTC.seconds - RETENTION_SECONDS)
        val checkInsToDelete = checkInRepository.allCheckIns.first()
            .filter {
                it.checkInEnd.seconds < retentionThreshold
            }
        checkInRepository.deleteCheckIns(checkInsToDelete)
    }

    companion object {
        private const val RETENTION_DAYS = 15
        private const val RETENTION_SECONDS = RETENTION_DAYS * DateTimeConstants.SECONDS_PER_DAY
    }
}
