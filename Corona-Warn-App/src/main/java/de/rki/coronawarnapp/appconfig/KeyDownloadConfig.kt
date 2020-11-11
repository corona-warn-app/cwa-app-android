package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalTime

interface KeyDownloadConfig {

    val individualDownloadTimeout: Duration

    val overallDownloadTimeout: Duration

    val revokedDayPackages: Collection<RevokedKeyPackage.Day>

    val revokedHourPackages: Collection<RevokedKeyPackage.Hour>

    interface RevokedKeyPackage {
        val etag: String
        val region: LocationCode

        interface Day : RevokedKeyPackage {
            val day: LocalDate
        }

        interface Hour : Day, RevokedKeyPackage {
            val hour: LocalTime
        }
    }

    interface Mapper : ConfigMapper<KeyDownloadConfig>
}
