package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.PresenceTracingRiskCalculationParamContainer
import de.rki.coronawarnapp.risk.DefaultRiskLevels.Companion.inRange
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.mapToRiskState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceTracingRiskMapper @Inject constructor(
    private val configProvider: AppConfigProvider
) {
    private var presenceTracingRiskCalculationParamContainer: PresenceTracingRiskCalculationParamContainer? = null

    private val mutex = Mutex()

    suspend fun clearConfig() {
        mutex.withLock {
            Timber.tag(TAG).i("Clearing config params.")
            presenceTracingRiskCalculationParamContainer = null
        }
    }

    suspend fun lookupTransmissionRiskValue(transmissionRiskLevel: Int): Double {
        return getTransmissionRiskValueMapping().find {
            (it.transmissionRiskLevel == transmissionRiskLevel)
        }?.transmissionRiskValue ?: 0.0
    }

    suspend fun lookupRiskStatePerDay(normalizedTime: Double): RiskState {
        return getNormalizedTimePerDayToRiskLevelMapping().find {
            it.normalizedTimeRange.inRange(normalizedTime)
        }
            ?.riskLevel
            ?.mapToRiskState() ?: RiskState.CALCULATION_FAILED
    }

    suspend fun lookupRiskStatePerCheckIn(normalizedTime: Double): RiskState {
        return getNormalizedTimePerCheckInToRiskLevelMapping().find {
            it.normalizedTimeRange.inRange(normalizedTime)
        }
            ?.riskLevel
            ?.mapToRiskState() ?: RiskState.CALCULATION_FAILED
    }

    private suspend fun getTransmissionRiskValueMapping() =
        getRiskCalculationParameters().transmissionRiskValueMapping

    private suspend fun getNormalizedTimePerDayToRiskLevelMapping() =
        getRiskCalculationParameters().normalizedTimePerDayToRiskLevelMapping

    private suspend fun getNormalizedTimePerCheckInToRiskLevelMapping() =
        getRiskCalculationParameters().normalizedTimePerCheckInToRiskLevelMapping

    private suspend fun getRiskCalculationParameters(): PresenceTracingRiskCalculationParamContainer = mutex.withLock {
        presenceTracingRiskCalculationParamContainer.let {
            if (it == null) {
                val newParams = configProvider.currentConfig.first().presenceTracing.riskCalculationParameters
                Timber.d("New params %s", newParams)
                presenceTracingRiskCalculationParamContainer = newParams
                newParams
            } else {
                it
            }
        }
    }
}

private const val TAG = "PtRiskMapper"
