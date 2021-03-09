package de.rki.coronawarnapp.eventregistration.checkins.download

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn

interface CheckInsPackage {

    /**
     * Hides the file reading
     */
    suspend fun extractCheckIns(): List<CheckIn>
}
