package de.rki.coronawarnapp.presencetracing.warning.storage

import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import java.io.File

data class TraceWarningPackageContainer(
    override val packageId: WarningPackageId,
    private val packagePath: File,
) : TraceWarningPackage {

    private val warningPackage by lazy<TraceWarning.TraceWarningPackage> {
        if (packagePath.exists()) {
            TraceWarning.TraceWarningPackage.parseFrom(packagePath.readBytes())
        } else {
            TraceWarning.TraceWarningPackage.getDefaultInstance()
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun extractUnencryptedWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
        return warningPackage.timeIntervalWarningsList
    }

    override suspend fun extractEncryptedWarnings(): List<CheckInOuterClass.CheckInProtectedReport> {
        return warningPackage.checkInProtectedReportsList
    }
}
