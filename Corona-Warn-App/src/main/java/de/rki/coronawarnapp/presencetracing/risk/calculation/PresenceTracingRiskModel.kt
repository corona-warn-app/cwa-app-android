package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.presencetracing.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.util.toLocalDateUtc
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
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
    val traceWarningPackageId: String,
    val startTime: Instant,
    val endTime: Instant
) {
    val localDateUtc = startTime.toLocalDateUtc()
    val overlap: Duration = Duration.of(
        max((endTime.toEpochMilli() - startTime.toEpochMilli()), 0),
        ChronoUnit.MILLIS
    )
    val roundedMinutes = (overlap.toMillis().toDouble() / TimeUnit.MINUTES.toMillis(1).toDouble()).roundToLong()
    fun normalizedTime(transmissionRiskValue: Double) = transmissionRiskValue * roundedMinutes
}

data class PresenceTracingDayRisk(
    val localDateUtc: LocalDate,
    val riskState: RiskState
)
