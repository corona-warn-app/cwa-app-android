package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

data class AggregatedRiskResult(
    val totalRiskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel =
        RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.UNSPECIFIED,
    val totalMinimumDistinctEncountersWithLowRisk: Int = 0,
    val totalMinimumDistinctEncountersWithHighRisk: Int = 0,
    val mostRecentDateWithLowRisk: Long?,
    val mostRecentDateWithHighRisk: Long?
)
