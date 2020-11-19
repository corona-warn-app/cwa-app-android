package de.rki.coronawarnapp.appconfig.mapping

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.KeyDownloadParameters.KeyDownloadParametersAndroid
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import javax.inject.Inject

@Reusable
class KeyDownloadParametersMapper @Inject constructor() : KeyDownloadConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): KeyDownloadConfig {
        val rawParameters = if (rawConfig.hasKeyDownloadParameters()) {
            rawConfig.keyDownloadParameters
        } else {
            null
        }

        return KeyDownloadConfigContainer(
            individualDownloadTimeout = rawParameters.individualTimeout(),
            overallDownloadTimeout = rawParameters.overAllTimeout(),
            revokedDayPackages = rawParameters.mapDayEtags(),
            revokedHourPackages = rawParameters.mapHourEtags()
        )
    }

    // If we are outside the valid data range, fallback to default value.
    private fun KeyDownloadParametersAndroid?.individualTimeout(): Duration =
        if (this == null || downloadTimeoutInSeconds > 1800 || downloadTimeoutInSeconds <= 0) {
            Duration.standardSeconds(60)
        } else {
            Duration.standardSeconds(downloadTimeoutInSeconds.toLong())
        }

    // If we are outside the valid data range, fallback to default value.
    private fun KeyDownloadParametersAndroid?.overAllTimeout(): Duration =
        if (this == null || overallTimeoutInSeconds > 1800 || overallTimeoutInSeconds <= 0) {
            Duration.standardMinutes(8)
        } else {
            Duration.standardSeconds(overallTimeoutInSeconds.toLong())
        }

    private fun KeyDownloadParametersAndroid?.mapDayEtags(): List<RevokedKeyPackage.Day> {
        if (this == null) return emptyList()

        return this.revokedDayPackagesList.mapNotNull {
            try {
                RevokedKeyPackage.Day(
                    etag = it.etag,
                    region = LocationCode(it.region),
                    day = LocalDate.parse(it.date, DAY_FORMATTER)
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse revoked day metadata: %s", it)
                null
            }
        }
    }

    private fun KeyDownloadParametersAndroid?.mapHourEtags(): List<RevokedKeyPackage.Hour> {
        if (this == null) return emptyList()

        return this.revokedHourPackagesList.mapNotNull {
            try {
                RevokedKeyPackage.Hour(
                    etag = it.etag,
                    region = LocationCode(it.region),
                    day = LocalDate.parse(it.date, DAY_FORMATTER),
                    hour = LocalTime.parse("${it.hour}", HOUR_FORMATTER)
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse revoked hour metadata: %s", it)
                null
            }
        }
    }

    data class KeyDownloadConfigContainer(
        override val individualDownloadTimeout: Duration,
        override val overallDownloadTimeout: Duration,
        override val revokedDayPackages: Collection<KeyDownloadConfig.RevokedKeyPackage.Day>,
        override val revokedHourPackages: Collection<KeyDownloadConfig.RevokedKeyPackage.Hour>
    ) : KeyDownloadConfig

    companion object {
        private val DAY_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd")
        private val HOUR_FORMATTER = DateTimeFormat.forPattern("H")
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal sealed class RevokedKeyPackage : KeyDownloadConfig.RevokedKeyPackage {

    data class Day(
        override val etag: String,
        override val region: LocationCode,
        override val day: LocalDate
    ) : RevokedKeyPackage(), KeyDownloadConfig.RevokedKeyPackage.Day

    data class Hour(
        override val etag: String,
        override val region: LocationCode,
        override val day: LocalDate,
        override val hour: LocalTime
    ) : RevokedKeyPackage(), KeyDownloadConfig.RevokedKeyPackage.Hour
}
