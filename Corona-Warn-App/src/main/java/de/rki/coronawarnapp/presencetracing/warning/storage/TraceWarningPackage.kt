package de.rki.coronawarnapp.presencetracing.warning.storage

import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning

interface TraceWarningPackage {

    val packageId: WarningPackageId

    /**
     * May throw an exception if there is an issue with the protobuf
     */
    suspend fun extractWarnings(): List<TraceWarning.TraceTimeIntervalWarning>
}
