package de.rki.coronawarnapp.service.riskscoreparameter

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.RiskScoreParameters
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants

object RiskScoreParameterService {
    suspend fun asyncRetrieveRiskScoreParameters(): ExposureConfiguration =
        WebRequestBuilder
            .asyncGetApplicationConfigurationFromServer(DiagnosisKeyConstants.COUNTRY_APPCONFIG_DOWNLOAD_URL)
            .configValues()
            .mapRiskScoreToExposureConfiguration()

    private fun ApplicationConfigurationOuterClass.ApplicationConfiguration.configValues():
            Pair<RiskScoreParameters, Int> = Pair(this.exposureConfig, this.minRiskScore)

    private fun Pair<RiskScoreParameters, Int>.mapRiskScoreToExposureConfiguration():
            ExposureConfiguration = ExposureConfiguration
        .ExposureConfigurationBuilder()
        .setMinimumRiskScore(this.second)
        .setAttenuationWeight(this.first.attenuationWeight.toInt())
        .setAttenuationScores(
            this.first.attenuation.gt73DbmValue,
            this.first.attenuation.gt63Le73DbmValue,
            this.first.attenuation.gt51Le63DbmValue,
            this.first.attenuation.gt33Le51DbmValue,
            this.first.attenuation.gt27Le33DbmValue,
            this.first.attenuation.gt15Le27DbmValue,
            this.first.attenuation.gt10Le15DbmValue,
            this.first.attenuation.lt10DbmValue
        )
        .setDaysSinceLastExposureWeight(this.first.daysWeight.toInt())
        .setDaysSinceLastExposureScores(
            this.first.daysSinceLastExposure.ge14DaysValue,
            this.first.daysSinceLastExposure.ge12Lt14DaysValue,
            this.first.daysSinceLastExposure.ge10Lt12DaysValue,
            this.first.daysSinceLastExposure.ge8Lt10DaysValue,
            this.first.daysSinceLastExposure.ge6Lt8DaysValue,
            this.first.daysSinceLastExposure.ge4Lt6DaysValue,
            this.first.daysSinceLastExposure.ge2Lt4DaysValue,
            this.first.daysSinceLastExposure.ge0Lt2DaysValue
        )
        .setDurationWeight(this.first.durationWeight.toInt())
        .setDurationScores(
            this.first.duration.eq0MinValue,
            this.first.duration.gt0Le5MinValue,
            this.first.duration.gt5Le10MinValue,
            this.first.duration.gt10Le15MinValue,
            this.first.duration.gt15Le20MinValue,
            this.first.duration.gt20Le25MinValue,
            this.first.duration.gt25Le30MinValue,
            this.first.duration.gt30MinValue
        )
        .setTransmissionRiskWeight(this.first.transmissionWeight.toInt())
        .setTransmissionRiskScores(
            this.first.transmission.appDefined1Value,
            this.first.transmission.appDefined2Value,
            this.first.transmission.appDefined3Value,
            this.first.transmission.appDefined4Value,
            this.first.transmission.appDefined5Value,
            this.first.transmission.appDefined6Value,
            this.first.transmission.appDefined7Value,
            this.first.transmission.appDefined8Value
        ).build()
}
