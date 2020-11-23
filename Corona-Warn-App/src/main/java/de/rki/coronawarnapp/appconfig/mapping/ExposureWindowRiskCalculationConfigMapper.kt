package de.rki.coronawarnapp.appconfig.mapping

import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import javax.inject.Inject

@Reusable
class ExposureWindowRiskCalculationConfigMapper @Inject constructor() :
    ExposureWindowRiskCalculationConfig.Mapper {

    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): ExposureWindowRiskCalculationConfig {
        if (!rawConfig.hasRiskCalculationParameters()) {
            throw ApplicationConfigurationInvalidException(
                message = "Risk Calculation Parameters are missing"
            )
        }

        if (!rawConfig.hasDiagnosisKeysDataMapping()) {
            throw ApplicationConfigurationInvalidException(
                message = "Diagnosis Keys Data Mapping is missing"
            )
        }

        val riskCalculationParameters = rawConfig.riskCalculationParameters

        return ExposureWindowRiskCalculationContainer(
            minutesAtAttenuationFilters = riskCalculationParameters
                .minutesAtAttenuationFiltersList,
            minutesAtAttenuationWeights = riskCalculationParameters
                .minutesAtAttenuationWeightsList,
            transmissionRiskLevelEncoding = riskCalculationParameters
                .trlEncoding,
            transmissionRiskLevelFilters = riskCalculationParameters
                .trlFiltersList,
            transmissionRiskLevelMultiplier = riskCalculationParameters
                .transmissionRiskLevelMultiplier,
            normalizedTimePerExposureWindowToRiskLevelMapping = riskCalculationParameters
                .normalizedTimePerEWToRiskLevelMappingList,
            normalizedTimePerDayToRiskLevelMappingList = riskCalculationParameters
                .normalizedTimePerDayToRiskLevelMappingList,
            diagnosisKeysDataMapping = rawConfig.diagnosisKeysDataMapping()
        )
    }

    private fun AppConfigAndroid.ApplicationConfigurationAndroid.diagnosisKeysDataMapping():
        DiagnosisKeysDataMapping {
        val diagnosisKeyDataMapping = this.diagnosisKeysDataMapping
        return DiagnosisKeysDataMapping.DiagnosisKeysDataMappingBuilder()
            .apply {
                setDaysSinceOnsetToInfectiousness(diagnosisKeyDataMapping.daysSinceOnsetToInfectiousnessMap)
                setInfectiousnessWhenDaysSinceOnsetMissing(
                    diagnosisKeysDataMapping.infectiousnessWhenDaysSinceOnsetMissing
                )
                setReportTypeWhenMissing(diagnosisKeysDataMapping.reportTypeWhenMissing)
            }.build()
    }

    data class ExposureWindowRiskCalculationContainer(
        override val minutesAtAttenuationFilters: List<RiskCalculationParametersOuterClass.MinutesAtAttenuationFilter>,
        override val minutesAtAttenuationWeights: List<RiskCalculationParametersOuterClass.MinutesAtAttenuationWeight>,
        override val transmissionRiskLevelEncoding: RiskCalculationParametersOuterClass.TransmissionRiskLevelEncoding,
        override val transmissionRiskLevelFilters: List<RiskCalculationParametersOuterClass.TrlFilter>,
        override val transmissionRiskLevelMultiplier: Double,
        override val normalizedTimePerExposureWindowToRiskLevelMapping:
        List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>,
        override val normalizedTimePerDayToRiskLevelMappingList:
        List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>,
        override val diagnosisKeysDataMapping: DiagnosisKeysDataMapping
    ) : ExposureWindowRiskCalculationConfig
}
