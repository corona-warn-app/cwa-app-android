package de.rki.coronawarnapp.presencetracing.warning

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning

interface TraceTimeIntervalWarningPackage {
    /**
     * Hides the file reading
     */
    suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning>

    val id: Long
}
