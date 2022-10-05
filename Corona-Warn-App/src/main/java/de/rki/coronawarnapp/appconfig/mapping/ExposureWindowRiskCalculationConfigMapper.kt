package de.rki.coronawarnapp.appconfig.mapping

import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import timber.log.Timber
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

        if (riskCalculationParameters.transmissionRiskValueMappingList.isEmpty()) {
            val msg = "Transmission Risk Value Mapping List is empty which indicates an outdated app config"
            Timber.w(msg)
            throw ApplicationConfigurationInvalidException(
                message = msg
            )
        }

        return ExposureWindowRiskCalculationContainer(
            minutesAtAttenuationFilters = riskCalculationParameters.minutesAtAttenuationFiltersList,
            minutesAtAttenuationWeights = riskCalculationParameters.minutesAtAttenuationWeightsList,
            transmissionRiskLevelEncoding = riskCalculationParameters.trlEncoding,
            transmissionRiskLevelFilters = riskCalculationParameters.trlFiltersList,
            normalizedTimePerExposureWindowToRiskLevelMapping =
            riskCalculationParameters.normalizedTimePerEWToRiskLevelMappingList,
            normalizedTimePerDayToRiskLevelMappingList =
            riskCalculationParameters.normalizedTimePerDayToRiskLevelMappingList,
            transmissionRiskValueMapping = riskCalculationParameters.transmissionRiskValueMappingList,
            diagnosisKeysDataMapping = rawConfig.diagnosisKeysDataMapping(),
            maxEncounterAgeInDays = riskCalculationParameters.getMaxEwAgeInDaysWithDefault()
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
        override val minutesAtAttenuationFilters:
            List<RiskCalculationParametersOuterClass.MinutesAtAttenuationFilter>,
        override val minutesAtAttenuationWeights:
            List<RiskCalculationParametersOuterClass.MinutesAtAttenuationWeight>,
        override val transmissionRiskLevelEncoding:
            RiskCalculationParametersOuterClass.TransmissionRiskLevelEncoding,
        override val transmissionRiskLevelFilters:
            List<RiskCalculationParametersOuterClass.TrlFilter>,
        override val normalizedTimePerExposureWindowToRiskLevelMapping:
            List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>,
        override val normalizedTimePerDayToRiskLevelMappingList:
            List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>,
        override val transmissionRiskValueMapping:
            List<RiskCalculationParametersOuterClass.TransmissionRiskValueMapping>,
        override val diagnosisKeysDataMapping: DiagnosisKeysDataMapping,
        override val maxEncounterAgeInDays: Long
    ) : ExposureWindowRiskCalculationConfig
}

private fun RiskCalculationParametersOuterClass.RiskCalculationParameters.getMaxEwAgeInDaysWithDefault(): Long =
    if (maxEncounterAgeInDays > 0) maxEncounterAgeInDays.toLong() else DEFAULT_EW_AGE_IN_DAYS

private const val DEFAULT_EW_AGE_IN_DAYS = 14L
