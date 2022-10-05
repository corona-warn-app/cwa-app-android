package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.risk.ProtoRiskLevel
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import java.time.Instant

data class EwAggregatedRiskResult(
    val totalRiskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel,
    val totalMinimumDistinctEncountersWithLowRisk: Int,
    val totalMinimumDistinctEncountersWithHighRisk: Int,
    val mostRecentDateWithLowRisk: Instant?,
    val mostRecentDateWithHighRisk: Instant?,
    val numberOfDaysWithLowRisk: Int,
    val numberOfDaysWithHighRisk: Int,
    var exposureWindowDayRisks: List<ExposureWindowDayRisk>? = null
) {

    fun isIncreasedRisk(): Boolean = totalRiskLevel == ProtoRiskLevel.HIGH

    fun isLowRisk(): Boolean = totalRiskLevel == ProtoRiskLevel.LOW
}
