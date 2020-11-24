package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

data class AggregatedRiskPerDateResult(
    val dateMillisSinceEpoch: Long,
    val riskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel,
    val minimumDistinctEncountersWithLowRisk: Int,
    val minimumDistinctEncountersWithHighRisk: Int
)
