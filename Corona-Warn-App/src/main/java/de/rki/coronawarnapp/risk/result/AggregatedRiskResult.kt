package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import org.joda.time.Instant

data class AggregatedRiskResult(
    val totalRiskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel,
    val totalMinimumDistinctEncountersWithLowRisk: Int,
    val totalMinimumDistinctEncountersWithHighRisk: Int,
    val mostRecentDateWithLowRisk: Instant?,
    val mostRecentDateWithHighRisk: Instant?,
    val numberOfDaysWithLowRisk: Int,
    val numberOfDaysWithHighRisk: Int
)
