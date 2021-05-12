package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.risk.RiskState
import javax.inject.Inject

class PresenceTracingRiskCalculator @Inject constructor(
    private val presenceTracingRiskMapper: PresenceTracingRiskMapper
) {
    suspend fun calculateNormalizedTime(overlaps: List<CheckInWarningOverlap>): List<CheckInNormalizedTime> {
        return overlaps.groupBy {
            it.checkInId
        }.flatMap { checkInGroup ->
            checkInGroup.value.groupBy {
                it.localDateUtc
            }.map { dateGroup ->
                val normalizedTimeSum = dateGroup.value.sumOf {
                    val value = presenceTracingRiskMapper.lookupTransmissionRiskValue(it.transmissionRiskLevel)
                    it.normalizedTime(value)
                }
                CheckInNormalizedTime(
                    checkInId = checkInGroup.key,
                    localDateUtc = dateGroup.key,
                    normalizedTime = normalizedTimeSum
                )
            }
        }
    }

    suspend fun calculateCheckInRiskPerDay(
        list: List<CheckInNormalizedTime>
    ): List<CheckInRiskPerDay> {
        return list.map {
            CheckInRiskPerDay(
                checkInId = it.checkInId,
                localDateUtc = it.localDateUtc,
                riskState = presenceTracingRiskMapper.lookupRiskStatePerCheckIn(it.normalizedTime)
            )
        }
    }

    suspend fun calculateDayRisk(
        list: List<CheckInNormalizedTime>
    ): List<PresenceTracingDayRisk> {
        return list.groupBy { it.localDateUtc }.map {
            val normalizedTimePerDate = it.value.sumOf {
                it.normalizedTime
            }
            PresenceTracingDayRisk(
                localDateUtc = it.key,
                riskState = presenceTracingRiskMapper.lookupRiskStatePerDay(normalizedTimePerDate)
            )
        }
    }

    suspend fun calculateTotalRisk(list: List<CheckInNormalizedTime>): RiskState {
        if (list.isEmpty()) return RiskState.LOW_RISK
        val riskPerDay = calculateCheckInRiskPerDay(list)
        if (riskPerDay.find { it.riskState == RiskState.INCREASED_RISK } != null)
            return RiskState.INCREASED_RISK
        if (riskPerDay.find { it.riskState == RiskState.LOW_RISK } != null)
            return RiskState.LOW_RISK
        return RiskState.CALCULATION_FAILED
    }
}
