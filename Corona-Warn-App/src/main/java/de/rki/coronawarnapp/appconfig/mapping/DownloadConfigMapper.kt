package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DownloadConfigMapper @Inject constructor() : KeyDownloadConfig.Mapper {
    override fun map(rawConfig: AppConfig.ApplicationConfiguration): KeyDownloadConfig {

        return KeyDownloadConfigContainer(
            invalidDayETags = rawConfig.androidKeyDownloadParameters.cachedDayPackagesToUpdateOnETagMismatchList.mapNotNull {
                try {
                    InvalidatedKeyFile.Day(
                        etag = it.etag,
                        region = LocationCode(it.region),
                        day = LocalDate.parse(it.date, DAY_FORMATTER)
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse invalidated day metadata: %s", it)
                    null
                }
            },
            invalidHourEtags = rawConfig.androidKeyDownloadParameters.cachedHourPackagesToUpdateOnETagMismatchList.mapNotNull {
                try {
                    InvalidatedKeyFile.Hour(
                        etag = it.etag,
                        region = LocationCode(it.region),
                        day = LocalDate.parse(it.date, DAY_FORMATTER),
                        hour = LocalTime.parse("${it.hour}", HOUR_FORMATTER)
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse invalidated hour metadata: %s", it)
                    null
                }
            }
        )
    }

    sealed class InvalidatedKeyFile : KeyDownloadConfig.InvalidatedKeyFile {

        data class Day(
            override val etag: String,
            override val region: LocationCode,
            override val day: LocalDate
        ) : InvalidatedKeyFile(), KeyDownloadConfig.InvalidatedKeyFile.Day

        data class Hour(
            override val etag: String,
            override val region: LocationCode,
            override val day: LocalDate,
            override val hour: LocalTime
        ) : InvalidatedKeyFile(), KeyDownloadConfig.InvalidatedKeyFile.Hour
    }

    data class KeyDownloadConfigContainer(
        override val invalidDayETags: Collection<KeyDownloadConfig.InvalidatedKeyFile.Day>,
        override val invalidHourEtags: Collection<KeyDownloadConfig.InvalidatedKeyFile.Hour>
    ) : KeyDownloadConfig

    companion object {
        private val DAY_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd")
        private val HOUR_FORMATTER = DateTimeFormat.forPattern("H")
    }
}
