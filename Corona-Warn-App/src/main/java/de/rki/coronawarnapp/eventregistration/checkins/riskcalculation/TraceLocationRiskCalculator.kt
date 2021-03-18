package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import org.joda.time.LocalDate
import javax.inject.Inject

class TraceLocationRiskCalculator @Inject constructor(
    private val traceLocationRiskMapper: TraceLocationRiskMapper
) {
    suspend fun calculateNormalizedTime(overlaps: List<CheckInOverlap>): List<TraceLocationCheckInNormalizedTime> {
        return overlaps.groupBy {
            it.checkInId
        }.flatMap { checkInGroup ->
            checkInGroup.value.groupBy {
                it.localDate
            }.map { dateGroup ->
                val normalizedTimeSum = dateGroup.value.fold(0.0) { sum, overlap ->
                    val value = traceLocationRiskMapper.lookupTransmissionRiskValue(overlap.transmissionRiskLevel)
                    sum + overlap.normalizedTime(value)
                }
                TraceLocationCheckInNormalizedTime(
                    checkInId = checkInGroup.key,
                    localDate = dateGroup.key,
                    normalizedTime = normalizedTimeSum
                )
            }
        }
    }

    suspend fun calculateRisk(list: List<TraceLocationCheckInNormalizedTime>): List<TraceLocationCheckInRiskPerDay> {
        return list.map {
            TraceLocationCheckInRiskPerDay(
                checkInId = it.checkInId,
                localDate = it.localDate,
                riskState = traceLocationRiskMapper.lookupRiskStatePerCheckIn(it.normalizedTime)
            )
        }
    }

    suspend fun calculateAggregatedRiskPerDay(list: List<TraceLocationCheckInNormalizedTime>): List<TracedLocationDayRisk> {
        return list.groupBy { it.localDate }.map {
            val normalizedTimePerDate = it.value.sumByDouble {
                it.normalizedTime
            }
            TracedLocationDayRisk(
                localDate = it.key,
                riskState = traceLocationRiskMapper.lookupRiskStatePerDay(normalizedTimePerDate)
            )
        }
    }

    suspend fun calculateAggregatedRiskPerCheckIn(list: List<TraceLocationCheckInNormalizedTime>): List<TracedLocationCheckInRisk> {
        return list.groupBy { it.checkInId }.map {
            val normalizedTimePerDate = it.value.sumByDouble {
                it.normalizedTime
            }
            TracedLocationCheckInRisk(
                checkInId = it.key,
                riskState = traceLocationRiskMapper.lookupRiskStatePerCheckIn(normalizedTimePerDate)
            )
        }
    }
}

private fun CheckInOverlap.normalizedTime(transmissionRiskValue: Double) =
    transmissionRiskValue * overlap.standardMinutes

data class TraceLocationCheckInNormalizedTime(
    val checkInId: Long,
    val localDate: LocalDate,
    val normalizedTime: Double
)

data class TraceLocationCheckInRiskPerDay(
    override val checkInId: Long,
    override val localDate: LocalDate,
    override val riskState: RiskState
) : TraceLocationCheckInRisk

data class TracedLocationDayRisk(
    val localDate: LocalDate,
    val riskState: RiskState
)

data class TracedLocationCheckInRisk(
    val checkInId: Long,
    val riskState: RiskState
)
