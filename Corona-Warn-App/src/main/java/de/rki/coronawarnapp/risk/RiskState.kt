package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel

enum class RiskState {
    LOW_RISK,
    INCREASED_RISK,
    CALCULATION_FAILED;
    fun isSuccessfulCalculation(): Boolean {
        return when(this) {
            LOW_RISK, INCREASED_RISK -> true
            else -> false
        }
    }
}

fun RiskLevel.mapToRiskState(): RiskState {
    return when (this) {
        RiskLevel.UNSPECIFIED, RiskLevel.UNRECOGNIZED -> RiskState.CALCULATION_FAILED
        RiskLevel.LOW -> RiskState.LOW_RISK
        RiskLevel.HIGH -> RiskState.INCREASED_RISK
    }
}
