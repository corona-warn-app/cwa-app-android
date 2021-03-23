package de.rki.coronawarnapp.eventregistration.checkins.checkout

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.util.TimeStamper
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckOutHandler @Inject constructor(
    private val repository: CheckInRepository,
    private val timeStamper: TimeStamper
) {
    /**
     * Throw **[IllegalArgumentException]** if the check-in does not exist.
     * Could happen on raceconditions, you should catch this, should be rare though.
     */
    suspend fun checkOut(checkInId: Long) {
        Timber.d("checkOut(checkInId=$checkInId)")
        val now = timeStamper.nowUTC

        var createJournalEntry = false
        repository.updateCheckIn(checkInId) {
            createJournalEntry = it.createJournalEntry
            it.copy(
                checkInEnd = now,
                completed = true
            )
        }

        if (createJournalEntry) {
            Timber.d("Creating journal entry for $checkInId")
            // TODO Create journal entry
        }

        // Remove auto-checkout timer?
    }
}

