package de.rki.coronawarnapp.eventregistration.checkins.download

import de.rki.coronawarnapp.eventregistration.checkins.TraceLocationCheckIn

interface CheckInsPackage {

    /**
     * Hides the file reading
     */
    suspend fun extractCheckIns(): List<TraceLocationCheckIn>
}
