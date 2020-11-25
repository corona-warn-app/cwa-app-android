package de.rki.coronawarnapp.risk.storage.internal.riskresults

import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import java.util.UUID

fun RiskLevelResult.toPersistedRiskResult(
    id: String = UUID.randomUUID().toString()
) = PersistedRiskLevelResultDao(
    id = id,
    calculatedAt = calculatedAt,
    aggregatedRiskResult = aggregatedRiskResult?.toPersistedAggregatedRiskResult(),
    failureReason = failureReason
)

fun AggregatedRiskResult.toPersistedAggregatedRiskResult() = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
    totalRiskLevel = totalRiskLevel,
    totalMinimumDistinctEncountersWithLowRisk = totalMinimumDistinctEncountersWithLowRisk,
    totalMinimumDistinctEncountersWithHighRisk = totalMinimumDistinctEncountersWithHighRisk,
    mostRecentDateWithLowRisk = mostRecentDateWithLowRisk,
    mostRecentDateWithHighRisk = mostRecentDateWithHighRisk,
    numberOfDaysWithLowRisk = numberOfDaysWithLowRisk,
    numberOfDaysWithHighRisk = numberOfDaysWithHighRisk
)
