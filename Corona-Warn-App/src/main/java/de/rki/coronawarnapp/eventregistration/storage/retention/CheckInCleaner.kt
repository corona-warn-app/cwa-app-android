package de.rki.coronawarnapp.eventregistration.storage.retention

import dagger.Reusable
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeConstants
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class CheckInCleaner @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val timeStamper: TimeStamper
) {

    suspend fun cleanUp() {
        val checkInsToDelete = checkInRepository.allCheckIns.first()
            .filter {
                isOutOfRetention(it.checkInEnd)
            }
        checkInRepository.deleteCheckIns(checkInsToDelete)
    }

    private fun isOutOfRetention(checkInEndDate: Instant): Boolean {
        val retentionThreshold = (timeStamper.nowUTC.seconds - (RETENTION_DAYS * DateTimeConstants.SECONDS_PER_DAY))
        return checkInEndDate.seconds < retentionThreshold
    }

    companion object {
        const val RETENTION_DAYS = 15
    }
}
