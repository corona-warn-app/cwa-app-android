package de.rki.coronawarnapp.service.riskscoreparameter

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.RiskScoreParameters
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants

object RiskScoreParameterService {
    suspend fun asyncRetrieveRiskScoreParameters(): ExposureConfiguration =
        WebRequestBuilder
            .asyncGetApplicationConfigurationFromServer(DiagnosisKeyConstants.COUNTRY_APPCONFIG_DOWNLOAD_URL)
            .exposureConfig
            .mapRiskScoreToExposureConfiguration()

    // TODO also add minimumRiskScore as soon as the backend will provide it in the correct proto
    // right now it is available but only in the parent ApplicationConfiguration
    private fun RiskScoreParameters.mapRiskScoreToExposureConfiguration(): ExposureConfiguration =
        ExposureConfiguration
            .ExposureConfigurationBuilder()
            .setAttenuationWeight(this.attenuationWeight.toInt())
            .setAttenuationScores(
                this.attenuation.gt73DbmValue,
                this.attenuation.gt63Le73DbmValue,
                this.attenuation.gt51Le63DbmValue,
                this.attenuation.gt33Le51DbmValue,
                this.attenuation.gt27Le33DbmValue,
                this.attenuation.gt15Le27DbmValue,
                this.attenuation.gt10Le15DbmValue,
                this.attenuation.lt10DbmValue
            )
            .setDaysSinceLastExposureWeight(this.daysWeight.toInt())
            .setDaysSinceLastExposureScores(
                this.daysSinceLastExposure.ge14DaysValue,
                this.daysSinceLastExposure.ge12Lt14DaysValue,
                this.daysSinceLastExposure.ge10Lt12DaysValue,
                this.daysSinceLastExposure.ge8Lt10DaysValue,
                this.daysSinceLastExposure.ge6Lt8DaysValue,
                this.daysSinceLastExposure.ge4Lt6DaysValue,
                this.daysSinceLastExposure.ge2Lt4DaysValue,
                this.daysSinceLastExposure.ge0Lt2DaysValue
            )
            .setDurationWeight(this.durationWeight.toInt())
            .setDurationScores(
                this.duration.eq0MinValue,
                this.duration.gt0Le5MinValue,
                this.duration.gt5Le10MinValue,
                this.duration.gt10Le15MinValue,
                this.duration.gt15Le20MinValue,
                this.duration.gt20Le25MinValue,
                this.duration.gt25Le30MinValue,
                this.duration.gt30MinValue
            )
            .setTransmissionRiskWeight(this.transmissionWeight.toInt())
            .setTransmissionRiskScores(
                this.transmission.appDefined1Value,
                this.transmission.appDefined2Value,
                this.transmission.appDefined3Value,
                this.transmission.appDefined4Value,
                this.transmission.appDefined5Value,
                this.transmission.appDefined6Value,
                this.transmission.appDefined7Value,
                this.transmission.appDefined8Value
            ).build()
}
