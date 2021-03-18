package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckInRiskMapper @Inject constructor(
    private val configProvider: AppConfigProvider
) {

    private var transmissionRiskValueMapping: List<RiskCalculationParametersOuterClass.TransmissionRiskValueMapping>? =
        null

    suspend fun lookupTransmissionRiskValue(transmissionRiskLevel: Int): Double {
        getTransmissionRiskMapping()
        return transmissionRiskValueMapping?.find {
            (it.transmissionRiskLevel == transmissionRiskLevel)
        }?.transmissionRiskValue ?: 0.0
    }

    suspend fun lookupRiskLevel(normalizedTime: Double) {
        getRiskLevelMapping()
        // TODO mapping
    }

    private suspend fun getTransmissionRiskMapping() {
        if (transmissionRiskValueMapping == null)
            transmissionRiskValueMapping = configProvider.currentConfig.first().transmissionRiskValueMapping
    }

    private suspend fun getRiskLevelMapping() {
        //configProvider.currentConfig.first().normalizedTimePerCheckInToRiskLevelMapping
    }
}

fun CheckInOverlap.normalizeTime(transmissionRiskValue: Double): Double =
    overlap.standardMinutes * transmissionRiskValue
