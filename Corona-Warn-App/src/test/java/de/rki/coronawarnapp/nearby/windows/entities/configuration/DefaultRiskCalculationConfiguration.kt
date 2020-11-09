package de.rki.coronawarnapp.nearby.windows.entities.configuration


import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.AppFeaturesOuterClass
import de.rki.coronawarnapp.server.protocols.internal.AppVersionConfig
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.ExposureDetectionParameters
import de.rki.coronawarnapp.server.protocols.internal.KeyDownloadParameters
import de.rki.coronawarnapp.server.protocols.internal.RiskScoreClassificationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import org.joda.time.Duration
import org.joda.time.Instant

data class DefaultRiskCalculationConfiguration(
    @SerializedName("minutesAtAttenuationFilters")
    override val minutesAtAttenuationFilters: List<RiskCalculationParametersOuterClass.MinutesAtAttenuationFilter>,
    @SerializedName("minutesAtAttenuationWeights")
    override val minutesAtAttenuationWeights: List<RiskCalculationParametersOuterClass.MinutesAtAttenuationWeight>,
    @SerializedName("normalizedTimePerDayToRiskLevelMapping")
    override val normalizedTimePerDayToRiskLevelMappingList:
    List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>,
    @SerializedName("normalizedTimePerEWToRiskLevelMapping")
    override val normalizedTimePerExposureWindowToRiskLevelMapping:
    List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>,
    @SerializedName("transmissionRiskLevelMultiplier")
    override val transmissionRiskLevelMultiplier: Double,
    @SerializedName("trlEncoding")
    override val transmissionRiskLevelEncoding: RiskCalculationParametersOuterClass.TransmissionRiskLevelEncoding,
    @SerializedName("trlFilters")
    override val transmissionRiskLevelFilters: List<RiskCalculationParametersOuterClass.TrlFilter>
) : ConfigData {
    override val updatedAt: Instant
        get() = TODO("Not yet implemented")
    override val localOffset: Duration
        get() = TODO("Not yet implemented")
    override val isFallback: Boolean
        get() = TODO("Not yet implemented")
    override val rawConfig: AppConfig.ApplicationConfiguration
        get() = TODO("Not yet implemented")
    override val appVersion: AppVersionConfig.ApplicationVersionConfiguration
        get() = TODO("Not yet implemented")
    override val supportedCountries: List<String>
        get() = TODO("Not yet implemented")
    override val appFeatureus: AppFeaturesOuterClass.AppFeatures
        get() = TODO("Not yet implemented")
    override val keyDownloadParameters: KeyDownloadParameters.KeyDownloadParametersAndroid
        get() = TODO("Not yet implemented")
    override val exposureDetectionConfiguration: ExposureConfiguration
        get() = TODO("Not yet implemented")
    override val exposureDetectionParameters: ExposureDetectionParameters.ExposureDetectionParametersAndroid
        get() = TODO("Not yet implemented")
    override val minRiskScore: Int
        get() = TODO("Not yet implemented")
    override val attenuationDuration: AttenuationDurationOuterClass.AttenuationDuration
        get() = TODO("Not yet implemented")
    override val riskScoreClasses: RiskScoreClassificationOuterClass.RiskScoreClassification
        get() = TODO("Not yet implemented")
}
