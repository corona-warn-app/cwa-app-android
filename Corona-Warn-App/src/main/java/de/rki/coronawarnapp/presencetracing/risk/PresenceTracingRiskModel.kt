package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import org.joda.time.DateTimeConstants
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate
import kotlin.math.max
import kotlin.math.roundToLong

data class CheckInNormalizedTime(
    val checkInId: Long,
    val localDateUtc: LocalDate,
    val normalizedTime: Double
)

data class CheckInRiskPerDay(
    override val checkInId: Long,
    override val localDateUtc: LocalDate,
    override val riskState: RiskState
) : TraceLocationCheckInRisk

data class CheckInWarningOverlap(
    val checkInId: Long,
    val transmissionRiskLevel: Int,
    val traceWarningPackageId: Long,
    val startTime: Instant,
    val endTime: Instant
) {
    val localDateUtc = startTime.toLocalDateUtc()
    val overlap: Duration = Duration(max((endTime.millis - startTime.millis), 0))
    val roundedMinutes = (overlap.millis.toDouble() / DateTimeConstants.MILLIS_PER_MINUTE.toDouble()).roundToLong()
    fun normalizedTime(transmissionRiskValue: Double) = transmissionRiskValue * roundedMinutes
}

data class PresenceTracingDayRisk(
    val localDateUtc: LocalDate,
    val riskState: RiskState
)
