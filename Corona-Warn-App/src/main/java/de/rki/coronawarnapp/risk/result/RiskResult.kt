package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

// TODO("Adjust Types")
data class RiskResult(
    val transmissionRiskLevel: Any,
    val normalizedTime: Int,
    val riskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel
)
