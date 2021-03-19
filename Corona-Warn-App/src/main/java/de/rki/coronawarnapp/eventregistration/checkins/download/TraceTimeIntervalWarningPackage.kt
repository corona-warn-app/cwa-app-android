package de.rki.coronawarnapp.eventregistration.checkins.download

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning

interface TraceTimeIntervalWarningPackage {

    /**
     * Hides the file reading
     */
    suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning>
}






