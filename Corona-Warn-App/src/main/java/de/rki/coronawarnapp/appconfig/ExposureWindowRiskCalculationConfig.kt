package de.rki.coronawarnapp.appconfig

import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

interface ExposureWindowRiskCalculationConfig {
    val minutesAtAttenuationFilters: List<RiskCalculationParametersOuterClass.MinutesAtAttenuationFilter>
    val minutesAtAttenuationWeights: List<RiskCalculationParametersOuterClass.MinutesAtAttenuationWeight>
    val transmissionRiskLevelEncoding: RiskCalculationParametersOuterClass.TransmissionRiskLevelEncoding
    val transmissionRiskLevelFilters: List<RiskCalculationParametersOuterClass.TrlFilter>
    val transmissionRiskLevelMultiplier: Double
    val normalizedTimePerExposureWindowToRiskLevelMapping:
        List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>
    val normalizedTimePerDayToRiskLevelMappingList:
        List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>
    val diagnosisKeysDataMapping: DiagnosisKeysDataMapping

    interface Mapper {
        fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): ExposureWindowRiskCalculationConfig
    }
}
