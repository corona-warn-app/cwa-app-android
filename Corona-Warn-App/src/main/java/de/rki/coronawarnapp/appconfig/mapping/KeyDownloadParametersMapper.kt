package de.rki.coronawarnapp.appconfig.mapping

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.KeyDownloadParameters.KeyDownloadParametersAndroid
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import javax.inject.Inject

@Reusable
class KeyDownloadParametersMapper @Inject constructor() : KeyDownloadConfig.Mapper {
    override fun map(rawConfig: AppConfig.ApplicationConfiguration): KeyDownloadConfig {
        val rawParameters = rawConfig.androidKeyDownloadParameters

        return KeyDownloadConfigContainer(
            individualDownloadTimeout = rawParameters.individualTimeout(),
            overallDownloadTimeout = rawParameters.overAllTimeout(),
            invalidDayETags = rawParameters.mapDayEtags(),
            invalidHourEtags = rawParameters.mapHourEtags()
        )
    }

    // If we are outside the valid data range, fallback to default value.
    private fun KeyDownloadParametersAndroid.individualTimeout(): Duration = when {
        downloadTimeoutInSeconds > 1800 -> Duration.standardSeconds(60)
        downloadTimeoutInSeconds <= 0 -> Duration.standardSeconds(60)
        else -> Duration.standardSeconds(downloadTimeoutInSeconds.toLong())
    }

    // If we are outside the valid data range, fallback to default value.
    private fun KeyDownloadParametersAndroid.overAllTimeout(): Duration = when {
        overallTimeoutInSeconds > 1800 -> Duration.standardMinutes(8)
        overallTimeoutInSeconds <= 0 -> Duration.standardMinutes(8)
        else -> Duration.standardSeconds(overallTimeoutInSeconds.toLong())
    }

    private fun KeyDownloadParametersAndroid.mapDayEtags(): List<InvalidatedKeyFile.Day> =
        this.revokedDayPackagesList.mapNotNull {
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
        }

    private fun KeyDownloadParametersAndroid.mapHourEtags(): List<InvalidatedKeyFile.Hour> =
        this.revokedHourPackagesList.mapNotNull {
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

    data class KeyDownloadConfigContainer(
        override val individualDownloadTimeout: Duration,
        override val overallDownloadTimeout: Duration,
        override val invalidDayETags: Collection<KeyDownloadConfig.InvalidatedKeyFile.Day>,
        override val invalidHourEtags: Collection<KeyDownloadConfig.InvalidatedKeyFile.Hour>
    ) : KeyDownloadConfig

    companion object {
        private val DAY_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd")
        private val HOUR_FORMATTER = DateTimeFormat.forPattern("H")
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal sealed class InvalidatedKeyFile : KeyDownloadConfig.InvalidatedKeyFile {

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
