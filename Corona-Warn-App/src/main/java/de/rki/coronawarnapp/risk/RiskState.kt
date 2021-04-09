package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel

enum class RiskState {
    LOW_RISK,
    INCREASED_RISK,
    CALCULATION_FAILED
}

fun RiskLevel.mapToRiskState(): RiskState {
    return when (this) {
        RiskLevel.UNSPECIFIED, RiskLevel.UNRECOGNIZED -> RiskState.CALCULATION_FAILED
        RiskLevel.LOW -> RiskState.LOW_RISK
        RiskLevel.HIGH -> RiskState.INCREASED_RISK
    }
}
