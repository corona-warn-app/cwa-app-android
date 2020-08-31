package de.rki.coronawarnapp.service.applicationconfiguration

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration
import de.rki.coronawarnapp.util.CWADebug

object ApplicationConfigurationService {
    suspend fun asyncRetrieveApplicationConfiguration(): ApplicationConfiguration {
        return WebRequestBuilder.getInstance().asyncGetApplicationConfigurationFromServer().let {
            return if (CWADebug.isDebugBuildOrMode) {
                // TODO: THIS IS A MOCK -> Remove after Backend is providing this information.
                it.toBuilder()
                    .clearSupportedCountries()
                    .addAllSupportedCountries(
                        listOf(
                            "DE", "UK", "FR", "IT", "SP", "PL", "RO", "NL",
                            "BE", "CZ", "EL", "SE", "PT", "HU", "AT", "CH", "BG", "DK", "FI", "SK",
                            "NO", "IE", "HR", "SI", "LT", "LV", "EE", "CY", "LU", "MT", "IS"
                        )
                    )
                    .build()
            } else {
                it
            }
        }
    }

    suspend fun asyncRetrieveExposureConfiguration(): ExposureConfiguration =
        asyncRetrieveApplicationConfiguration()
            .mapRiskScoreToExposureConfiguration()

    private fun ApplicationConfiguration.mapRiskScoreToExposureConfiguration(): ExposureConfiguration =
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
}
