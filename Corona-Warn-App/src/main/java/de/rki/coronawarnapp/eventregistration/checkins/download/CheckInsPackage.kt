package de.rki.coronawarnapp.eventregistration.checkins.download

import de.rki.coronawarnapp.eventregistration.checkins.EventCheckIn

interface CheckInsPackage {

    /**
     * Hides the file reading
     */
    suspend fun extractCheckIns(): List<EventCheckIn>
}
