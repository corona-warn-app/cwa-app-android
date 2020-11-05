package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import org.joda.time.LocalDate
import org.joda.time.LocalTime

interface KeyDownloadConfig {

    val invalidDayETags: Collection<InvalidatedKeyFile.Day>

    val invalidHourEtags: Collection<InvalidatedKeyFile.Hour>

    interface InvalidatedKeyFile {
        val etag: String
        val region: LocationCode

        interface Day : InvalidatedKeyFile {
            val day: LocalDate
        }

        interface Hour : Day, InvalidatedKeyFile {
            val hour: LocalTime
        }
    }

    interface Mapper : ConfigMapper<KeyDownloadConfig>
}
