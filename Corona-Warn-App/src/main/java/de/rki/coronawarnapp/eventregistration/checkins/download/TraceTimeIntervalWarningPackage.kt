package de.rki.coronawarnapp.eventregistration.checkins.download

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning

interface TraceTimeIntervalWarningPackage {

    /**
     * Hides the file reading
     */
    suspend fun extractTraceTimeIntervalWarnings(): List<TraceWarning.TraceTimeIntervalWarning>

    /**
     * Numeric identifier representing the hour since epoch, used in the Api endpoint
     */
    val warningPackageId: Long
}
