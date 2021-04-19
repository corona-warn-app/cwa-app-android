package de.rki.coronawarnapp.presencetracing.checkins.common

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import kotlinx.coroutines.flow.map

/**
 * Returns completed [CheckIn]s only
 */
val CheckInRepository.completedCheckIns
    get() = checkInsWithinRetention.map { checkIns ->
        checkIns.filter { checkIn -> checkIn.completed }
    }
