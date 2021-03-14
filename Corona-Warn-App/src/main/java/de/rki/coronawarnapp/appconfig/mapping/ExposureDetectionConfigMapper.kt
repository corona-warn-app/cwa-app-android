package de.rki.coronawarnapp.appconfig.mapping

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.ExposureDetectionParameters.ExposureDetectionParametersAndroid
import org.joda.time.Duration
import javax.inject.Inject

@Reusable
class ExposureDetectionConfigMapper @Inject constructor() : ExposureDetectionConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): ExposureDetectionConfig {
        val exposureParams = if (rawConfig.hasExposureDetectionParameters()) {
            rawConfig.exposureDetectionParameters
        } else {
            null
        }
        return ExposureDetectionConfigContainer(
            exposureDetectionParameters = exposureParams,
            maxExposureDetectionsPerUTCDay = exposureParams.maxExposureDetectionsPerDay(),
            minTimeBetweenDetections = exposureParams.minTimeBetweenExposureDetections(),
            overallDetectionTimeout = exposureParams.overAllDetectionTimeout()
        )
    }

    data class ExposureDetectionConfigContainer(
        override val exposureDetectionParameters: ExposureDetectionParametersAndroid?,
        override val maxExposureDetectionsPerUTCDay: Int,
        override val minTimeBetweenDetections: Duration,
        override val overallDetectionTimeout: Duration
    ) : ExposureDetectionConfig
}

// If we are outside the valid data range, fallback to default value.
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun ExposureDetectionParametersAndroid?.overAllDetectionTimeout(): Duration =
    if (this == null || overallTimeoutInSeconds > 3600 || overallTimeoutInSeconds <= 0) {
        Duration.standardMinutes(15)
    } else {
        Duration.standardSeconds(overallTimeoutInSeconds.toLong())
    }

// If we are outside the valid data range, fallback to default value.
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun ExposureDetectionParametersAndroid?.maxExposureDetectionsPerDay(): Int =
    if (this == null || maxExposureDetectionsPerInterval > 6 || maxExposureDetectionsPerInterval < 0) {
        6
    } else {
        maxExposureDetectionsPerInterval
    }

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun ExposureDetectionParametersAndroid?.minTimeBetweenExposureDetections(): Duration {
    val detectionsPerDay = this.maxExposureDetectionsPerDay()
    return if (detectionsPerDay == 0) {
        Duration.standardDays(1)
    } else {
        (24 / detectionsPerDay).let { Duration.standardHours(it.toLong()) }
    }
}
