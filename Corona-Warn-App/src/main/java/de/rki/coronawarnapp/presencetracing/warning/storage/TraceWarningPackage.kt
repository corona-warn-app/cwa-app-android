package de.rki.coronawarnapp.presencetracing.warning.storage

import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning

interface TraceWarningPackage {

    val packageId: WarningPackageId

    /**
     * May throw an exception if there is an issue with the protobuf
     */
    suspend fun extractUnencryptedWarnings(): List<TraceWarning.TraceTimeIntervalWarning>

    suspend fun extractEncryptedWarnings(): List<CheckInOuterClass.CheckInProtectedReport>
}
