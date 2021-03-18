package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import org.joda.time.LocalDate
import javax.inject.Inject

class CheckInRiskCalculator @Inject constructor(
    private val checkInRiskMapper: CheckInRiskMapper
) {
    suspend fun calculateNormalizedTime(overlaps: List<CheckInOverlap>): List<CheckInRisk> {
        return overlaps.groupBy {
            it.checkInId
        }.flatMap { checkInGroup ->
            checkInGroup.value.groupBy {
                it.localDate
            }.map { dateGroup ->
                val normalizedTimeSum = dateGroup.value.fold(0.0) { sum, overlap ->
                    val value = checkInRiskMapper.lookupTransmissionRiskValue(overlap.transmissionRiskLevel)
                    sum + overlap.normalizedTime(value)
                }
                CheckInRisk(
                    checkInId = checkInGroup.key,
                    localDate = dateGroup.key,
                    normalizedTime = normalizedTimeSum
                )
            }
        }
    }
}

private fun CheckInOverlap.normalizedTime(transmissionRiskValue: Double) =
    transmissionRiskValue * overlap.standardMinutes

data class CheckInRisk(
    val checkInId: Long,
    val localDate: LocalDate,
    val normalizedTime: Double
)
