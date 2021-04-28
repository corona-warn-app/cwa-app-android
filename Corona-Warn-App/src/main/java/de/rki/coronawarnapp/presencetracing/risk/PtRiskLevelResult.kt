package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningOverlap
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.risk.RiskState
import org.joda.time.Instant
import org.joda.time.LocalDate

/**
 * @param presenceTracingDayRisk Only available for the latest calculation, otherwise null
 * @param checkInWarningOverlaps Only available for the latest calculation, otherwise null
 */
data class PtRiskLevelResult(
    val calculatedAt: Instant,
    val riskState: RiskState,
    private val presenceTracingDayRisk: List<PresenceTracingDayRisk>? = null,
    private val checkInWarningOverlaps: List<CheckInWarningOverlap>? = null,
) {

    val wasSuccessfullyCalculated: Boolean by lazy {
        riskState != RiskState.CALCULATION_FAILED
    }

    val daysWithHighRisk: List<LocalDate> by lazy {
        presenceTracingDayRisk?.filter {
            it.riskState == RiskState.INCREASED_RISK
        }?.map { it.localDateUtc } ?: emptyList()
    }

    val daysWithLowRisk: List<LocalDate> by lazy {
        presenceTracingDayRisk?.filter {
            it.riskState == RiskState.LOW_RISK
        }?.map { it.localDateUtc } ?: emptyList()
    }

    val mostRecentDateWithHighRisk: LocalDate? by lazy {
        presenceTracingDayRisk
            ?.filter { it.riskState == RiskState.INCREASED_RISK }
            ?.maxByOrNull { it.localDateUtc }
            ?.localDateUtc
    }

    val mostRecentDateWithLowRisk: LocalDate? by lazy {
        presenceTracingDayRisk
            ?.filter { it.riskState == RiskState.LOW_RISK }
            ?.maxByOrNull { it.localDateUtc }
            ?.localDateUtc
    }

    val checkInOverlapCount: Int by lazy {
        checkInWarningOverlaps?.size ?: 0
    }
}
