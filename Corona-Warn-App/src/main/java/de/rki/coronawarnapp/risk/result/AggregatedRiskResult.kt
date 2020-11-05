package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

data class AggregatedRiskResult(
    val totalRiskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel,
    val totalMinimumDistinctEncountersWithLowRisk: Int,
    val totalMinimumDistinctEncountersWithHighRisk: Int,
    val mostRecentDateWithLowRisk: Long?,
    val mostRecentDateWithHighRisk: Long?
)
