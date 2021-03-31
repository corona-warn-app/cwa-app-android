package de.rki.coronawarnapp.presencetracing.warning.storage

import de.rki.coronawarnapp.presencetracing.warning.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import java.io.File

data class TraceWarningPackageContainer(
    override val id: WarningPackageId,
    val packagePath: File,
) : TraceTimeIntervalWarningPackage {
    override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
        TODO("Not yet implemented")
    }
}
