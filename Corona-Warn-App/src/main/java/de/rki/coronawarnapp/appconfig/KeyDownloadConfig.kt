package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

interface KeyDownloadConfig {

    val individualDownloadTimeout: Duration

    val overallDownloadTimeout: Duration

    val revokedDayPackages: Collection<RevokedKeyPackage.Day>

    val revokedHourPackages: Collection<RevokedKeyPackage.Hour>

    val revokedTraceWarningPackages: Collection<RevokedKeyPackage.TraceWarning>

    interface RevokedKeyPackage {
        val etag: String
        val region: LocationCode

        interface Day : RevokedKeyPackage {
            val day: LocalDate
        }

        interface Hour : Day, RevokedKeyPackage {
            val hour: LocalTime
        }

        interface TraceWarning : RevokedKeyPackage
    }

    interface Mapper : ConfigMapper<KeyDownloadConfig>
}
