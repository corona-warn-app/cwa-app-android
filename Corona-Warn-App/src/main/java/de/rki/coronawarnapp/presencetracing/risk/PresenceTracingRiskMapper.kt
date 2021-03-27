package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.PresenceTracingRiskCalculationParamContainer
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class PresenceTracingRiskMapper @Inject constructor(
    private val configProvider: AppConfigProvider
) {
    private var presenceTracingRiskCalculationParamContainer: PresenceTracingRiskCalculationParamContainer? = null

    suspend fun lookupTransmissionRiskValue(transmissionRiskLevel: Int): Double {
        return getTransmissionRiskValueMapping()?.find {
            (it.transmissionRiskLevel == transmissionRiskLevel)
        }?.transmissionRiskValue ?: 0.0
    }

    suspend fun lookupRiskStatePerDay(normalizedTime: Double): RiskState {
        return getNormalizedTimePerDayToRiskLevelMapping()?.find {
            it.normalizedTimeRange.isInRange(normalizedTime)
        }
            ?.riskLevel
            ?.mapToRiskState() ?: RiskState.CALCULATION_FAILED
    }

    suspend fun lookupRiskStatePerCheckIn(normalizedTime: Double): RiskState {
        return getNormalizedTimePerCheckInToRiskLevelMapping()?.find {
            it.normalizedTimeRange.isInRange(normalizedTime)
        }
            ?.riskLevel
            ?.mapToRiskState() ?: RiskState.CALCULATION_FAILED
    }

    private suspend fun getTransmissionRiskValueMapping() =
        getRiskCalculationParameters()?.transmissionRiskValueMapping

    private suspend fun getNormalizedTimePerDayToRiskLevelMapping() =
        getRiskCalculationParameters()?.normalizedTimePerDayToRiskLevelMapping

    private suspend fun getNormalizedTimePerCheckInToRiskLevelMapping() =
        getRiskCalculationParameters()?.normalizedTimePerCheckInToRiskLevelMapping

    private suspend fun getRiskCalculationParameters(): PresenceTracingRiskCalculationParamContainer? {
        if (presenceTracingRiskCalculationParamContainer == null) {
            presenceTracingRiskCalculationParamContainer =
                configProvider.currentConfig.first().presenceTracing.riskCalculationParameters
            Timber.d(presenceTracingRiskCalculationParamContainer.toString())
        }
        return presenceTracingRiskCalculationParamContainer
    }
}

private fun RiskCalculationParametersOuterClass.Range.isInRange(value: Double): Boolean {
    if (minExclusive && value <= min) return false
    if (!minExclusive && value < min) return false
    if (maxExclusive && value >= max) return false
    if (!maxExclusive && value > max) return false
    return true
}
