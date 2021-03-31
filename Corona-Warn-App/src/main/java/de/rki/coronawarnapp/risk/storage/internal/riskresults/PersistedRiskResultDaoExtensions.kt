package de.rki.coronawarnapp.risk.storage.internal.riskresults

import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import java.util.UUID

fun EwRiskLevelResult.toPersistedRiskResult(
    id: String = UUID.randomUUID().toString()
) = PersistedRiskLevelResultDao(
    id = id,
    calculatedAt = calculatedAt,
    aggregatedRiskResult = ewAggregatedRiskResult?.toPersistedAggregatedRiskResult(),
    failureReason = failureReason
)

fun EwAggregatedRiskResult.toPersistedAggregatedRiskResult() = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
    totalRiskLevel = totalRiskLevel,
    totalMinimumDistinctEncountersWithLowRisk = totalMinimumDistinctEncountersWithLowRisk,
    totalMinimumDistinctEncountersWithHighRisk = totalMinimumDistinctEncountersWithHighRisk,
    mostRecentDateWithLowRisk = mostRecentDateWithLowRisk,
    mostRecentDateWithHighRisk = mostRecentDateWithHighRisk,
    numberOfDaysWithLowRisk = numberOfDaysWithLowRisk,
    numberOfDaysWithHighRisk = numberOfDaysWithHighRisk
)
