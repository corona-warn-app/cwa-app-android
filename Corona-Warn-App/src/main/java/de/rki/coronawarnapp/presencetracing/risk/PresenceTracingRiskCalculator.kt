package de.rki.coronawarnapp.presencetracing.risk

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
                val normalizedTimeSum = dateGroup.value.sumByDouble {
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

    suspend fun calculateAggregatedRiskPerDay(list: List<CheckInNormalizedTime>):
        List<PresenceTracingDayRisk> {
            return list.groupBy { it.localDateUtc }.map {
                val normalizedTimePerDate = it.value.sumByDouble {
                    it.normalizedTime
                }
                PresenceTracingDayRisk(
                    localDateUtc = it.key,
                    riskState = presenceTracingRiskMapper.lookupRiskStatePerDay(normalizedTimePerDate)
                )
            }
        }
}
