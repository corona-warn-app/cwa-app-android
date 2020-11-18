package de.rki.coronawarnapp.appconfig.mapping

import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.ExposureDetectionParameters.ExposureDetectionParametersAndroid
import org.joda.time.Duration
import javax.inject.Inject

@Reusable
class ExposureDetectionConfigMapper @Inject constructor() : ExposureDetectionConfig.Mapper {
    override fun map(rawConfig: AppConfig.ApplicationConfiguration): ExposureDetectionConfig {
        val exposureParams = if (rawConfig.hasAndroidExposureDetectionParameters()) {
            rawConfig.androidExposureDetectionParameters
        } else {
            null
        }
        return ExposureDetectionConfigContainer(
            exposureDetectionConfiguration = rawConfig.mapRiskScoreToExposureConfiguration(),
            exposureDetectionParameters = exposureParams,
            maxExposureDetectionsPerUTCDay = exposureParams.maxExposureDetectionsPerDay(),
            minTimeBetweenDetections = exposureParams.minTimeBetweenExposureDetections(),
            overallDetectionTimeout = exposureParams.overAllDetectionTimeout()
        )
    }

    data class ExposureDetectionConfigContainer(
        override val exposureDetectionConfiguration: ExposureConfiguration,
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
        Duration.standardDays(99)
    } else {
        (24 / detectionsPerDay).let { Duration.standardHours(it.toLong()) }
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun AppConfig.ApplicationConfiguration.mapRiskScoreToExposureConfiguration(): ExposureConfiguration =
    ExposureConfiguration
        .ExposureConfigurationBuilder()
        .setTransmissionRiskScores(
            this.exposureConfig.transmission.appDefined1Value,
            this.exposureConfig.transmission.appDefined2Value,
            this.exposureConfig.transmission.appDefined3Value,
            this.exposureConfig.transmission.appDefined4Value,
            this.exposureConfig.transmission.appDefined5Value,
            this.exposureConfig.transmission.appDefined6Value,
            this.exposureConfig.transmission.appDefined7Value,
            this.exposureConfig.transmission.appDefined8Value
        )
        .setDurationScores(
            this.exposureConfig.duration.eq0MinValue,
            this.exposureConfig.duration.gt0Le5MinValue,
            this.exposureConfig.duration.gt5Le10MinValue,
            this.exposureConfig.duration.gt10Le15MinValue,
            this.exposureConfig.duration.gt15Le20MinValue,
            this.exposureConfig.duration.gt20Le25MinValue,
            this.exposureConfig.duration.gt25Le30MinValue,
            this.exposureConfig.duration.gt30MinValue
        )
        .setDaysSinceLastExposureScores(
            this.exposureConfig.daysSinceLastExposure.ge14DaysValue,
            this.exposureConfig.daysSinceLastExposure.ge12Lt14DaysValue,
            this.exposureConfig.daysSinceLastExposure.ge10Lt12DaysValue,
            this.exposureConfig.daysSinceLastExposure.ge8Lt10DaysValue,
            this.exposureConfig.daysSinceLastExposure.ge6Lt8DaysValue,
            this.exposureConfig.daysSinceLastExposure.ge4Lt6DaysValue,
            this.exposureConfig.daysSinceLastExposure.ge2Lt4DaysValue,
            this.exposureConfig.daysSinceLastExposure.ge0Lt2DaysValue
        )
        .setAttenuationScores(
            this.exposureConfig.attenuation.gt73DbmValue,
            this.exposureConfig.attenuation.gt63Le73DbmValue,
            this.exposureConfig.attenuation.gt51Le63DbmValue,
            this.exposureConfig.attenuation.gt33Le51DbmValue,
            this.exposureConfig.attenuation.gt27Le33DbmValue,
            this.exposureConfig.attenuation.gt15Le27DbmValue,
            this.exposureConfig.attenuation.gt10Le15DbmValue,
            this.exposureConfig.attenuation.le10DbmValue
        )
        .setMinimumRiskScore(this.minRiskScore)
        .setDurationAtAttenuationThresholds(
            this.attenuationDuration.thresholds.lower,
            this.attenuationDuration.thresholds.upper
        )
        .build()
