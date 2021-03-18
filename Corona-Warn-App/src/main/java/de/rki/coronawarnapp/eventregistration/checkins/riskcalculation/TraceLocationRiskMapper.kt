package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TraceLocationRiskMapper @Inject constructor(
    private val configProvider: AppConfigProvider
) {

    private var transmissionRiskValueMapping:
        List<RiskCalculationParametersOuterClass.TransmissionRiskValueMapping>? = null

    private var normalizedTimePerCheckInToRiskLevelMapping:
        List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>? = null

    private var normalizedTimePerDayToRiskLevelMapping:
        List<RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping>? = null

    suspend fun lookupTransmissionRiskValue(transmissionRiskLevel: Int): Double {
        getTransmissionRiskMapping()
        return transmissionRiskValueMapping?.find {
            (it.transmissionRiskLevel == transmissionRiskLevel)
        }?.transmissionRiskValue ?: 0.0
    }

    suspend fun lookupRiskStatePerDay(normalizedTime: Double): RiskState {
        getRiskLevelPerDayMapping()
        // TODO mapping
        return RiskState.LOW_RISK
    }

    suspend fun lookupRiskStatePerCheckIn(normalizedTime: Double): RiskState {
        getRiskLevelPerCheckInMapping()
        // TODO mapping
        return RiskState.LOW_RISK
    }

    private suspend fun getTransmissionRiskMapping() {
        if (transmissionRiskValueMapping == null)
            transmissionRiskValueMapping = configProvider.currentConfig.first().transmissionRiskValueMapping
    }

    private suspend fun getRiskLevelPerDayMapping() {
        if (normalizedTimePerDayToRiskLevelMapping == null)
            normalizedTimePerDayToRiskLevelMapping =
                configProvider.currentConfig.first().normalizedTimePerDayToRiskLevelMappingList
    }

    private suspend fun getRiskLevelPerCheckInMapping() {
        // TODO missing in config
//        if (normalizedTimePerCheckInToRiskLevelMapping == null)
//            normalizedTimePerCheckInToRiskLevelMapping = configProvider.currentConfig.first().normalizedTimePerCheckInToRiskLevelMapping
    }
}


