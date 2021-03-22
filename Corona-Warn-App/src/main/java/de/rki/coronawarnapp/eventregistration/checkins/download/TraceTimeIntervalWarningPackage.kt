package de.rki.coronawarnapp.eventregistration.checkins.download

interface TraceTimeIntervalWarningPackage {

    /**
     * Hides the file reading
     */
    suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning>
}
