package de.rki.coronawarnapp.eventregistration.checkins.checkout

import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckOutHandler @Inject constructor(
    private val repository: CheckInRepository,
    private val timeStamper: TimeStamper,
    private val diaryRepository: ContactDiaryRepository,
) {
    /**
     * Throw **[IllegalArgumentException]** if the check-in does not exist.
     * Could happen on raceconditions, you should catch this, should be rare though.
     */
    suspend fun checkOut(checkInId: Long, checkOutAt: Instant = timeStamper.nowUTC) {
        Timber.d("checkOut(checkInId=$checkInId)")

        var createJournalEntry = false
        repository.updateCheckIn(checkInId) {
            createJournalEntry = it.createJournalEntry
            it.copy(
                checkInEnd = checkOutAt,
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

