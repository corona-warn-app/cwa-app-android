package de.rki.coronawarnapp.presencetracing.warning.riskcalculation

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import org.joda.time.DateTimeConstants
import org.joda.time.Duration
import org.joda.time.LocalDate
import kotlin.math.roundToLong

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

data class CheckInOverlap(
    val checkInId: Long,
    val localDate: LocalDate,
    val overlap: Duration,
    val transmissionRiskLevel: Int
) {
    val roundedMinutes = (overlap.millis.toDouble() / DateTimeConstants.MILLIS_PER_MINUTE.toDouble()).roundToLong()
    fun normalizedTime(transmissionRiskValue: Double) = transmissionRiskValue * roundedMinutes
}
