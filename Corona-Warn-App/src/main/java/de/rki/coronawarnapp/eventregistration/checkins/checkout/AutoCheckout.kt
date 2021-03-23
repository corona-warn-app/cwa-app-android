package de.rki.coronawarnapp.eventregistration.checkins.checkout

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Knows when the party is over.
 * (╯°□°)╯︵ ┻━┻
 */
@Singleton
class AutoCheckout @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val checkOutHandler: CheckOutHandler
) {

    suspend fun enableAutoCheckOut(
        checkInId: Long
    ) {
        TODO()
    }

    suspend fun disableAutoCheckOut(
        checkInId: Long
    ) {
        TODO()
    }

    suspend fun refresh() {
        TODO()
    }
}
