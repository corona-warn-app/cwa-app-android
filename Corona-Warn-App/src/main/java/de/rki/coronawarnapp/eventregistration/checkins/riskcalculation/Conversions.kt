package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

// converts number of 10min intervals into milliseconds
internal fun Int.tenMinIntervalToMillis() = this * MILLIS_IN_MIN

private const val MILLIS_IN_MIN = 600L * 1000L

fun RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.mapToRiskState(): RiskState {
    return when (this) {
        RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.UNSPECIFIED, RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.UNRECOGNIZED -> RiskState.CALCULATION_FAILED
        RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.LOW -> RiskState.LOW_RISK
        RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH -> RiskState.INCREASED_RISK
    }
}
