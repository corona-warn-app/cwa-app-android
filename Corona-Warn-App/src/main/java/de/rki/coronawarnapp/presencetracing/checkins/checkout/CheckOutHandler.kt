package de.rki.coronawarnapp.presencetracing.checkins.checkout

import dagger.Reusable
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.util.TimeStamper
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

@Reusable
class CheckOutHandler @Inject constructor(
    private val repository: CheckInRepository,
    private val timeStamper: TimeStamper,
    private val contactJournalCheckInEntryCreator: ContactJournalCheckInEntryCreator
) {
    /**
     * Throw **[IllegalArgumentException]** if the check-in does not exist.
     * Could happen on raceconditions, you should catch this, should be rare though.
     */
    suspend fun checkOut(checkInId: Long, checkOutAt: Instant = timeStamper.nowUTC) {
        Timber.d("checkOut(checkInId=$checkInId, checkOutAt=%s)", checkOutAt)

        var checkIn: CheckIn? = null
        repository.updateCheckIn(checkInId) {
            it.copy(
                checkInEnd = checkOutAt,
                completed = true
            ).also { c -> checkIn = c }
        }

        if (checkIn?.createJournalEntry == true) {
            contactJournalCheckInEntryCreator.createEntry(checkIn!!)
        }

        // Remove auto-checkout timer?
    }
}
