package de.rki.coronawarnapp.presencetracing.warning.riskcalculation

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import org.joda.time.DateTimeConstants
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate
import kotlin.math.max
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

data class PresenceTracingDayRisk(
    val localDate: LocalDate,
    val riskState: RiskState
)

data class CheckInOverlap(
    val checkInId: Long,
    val transmissionRiskLevel: Int,
    val traceWarningPackageId: Long,
    val startTime: Instant,
    val endTime: Instant
) {
    val localDate = startTime.toLocalDate()
    val overlap: Duration = Duration(max((endTime.millis - startTime.millis), 0))
    val roundedMinutes = (overlap.millis.toDouble() / DateTimeConstants.MILLIS_PER_MINUTE.toDouble()).roundToLong()
    fun normalizedTime(transmissionRiskValue: Double) = transmissionRiskValue * roundedMinutes
}
