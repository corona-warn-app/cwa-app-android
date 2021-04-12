package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningOverlap
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.risk.RiskState
import org.joda.time.Instant
import org.joda.time.LocalDate

/**
 * @param presenceTracingDayRisk Only available for the last calculation, otherwise null
 * @param checkInWarningOverlaps Only available for the last calculation, otherwise null
 */
data class PtRiskLevelResult(
    val calculatedAt: Instant,
    val riskState: RiskState,
    private val presenceTracingDayRisk: List<PresenceTracingDayRisk>? = null,
    private val checkInWarningOverlaps: List<CheckInWarningOverlap>? = null,
) {

    val wasSuccessfullyCalculated: Boolean
        get() = riskState != RiskState.CALCULATION_FAILED

    val numberOfDaysWithHighRisk: Int
        get() = presenceTracingDayRisk?.count { it.riskState == RiskState.INCREASED_RISK } ?: 0

    val numberOfDaysWithLowRisk: Int
        get() = presenceTracingDayRisk?.count { it.riskState == RiskState.LOW_RISK } ?: 0

    val mostRecentDateWithHighRisk: LocalDate?
        get() = presenceTracingDayRisk
            ?.filter { it.riskState == RiskState.INCREASED_RISK }
            ?.maxByOrNull { it.localDateUtc }
            ?.localDateUtc

    val mostRecentDateWithLowRisk: LocalDate?
        get() = presenceTracingDayRisk
            ?.filter { it.riskState == RiskState.LOW_RISK }
            ?.maxByOrNull { it.localDateUtc }
            ?.localDateUtc

    val daysWithEncounters: Int
        get() = when (riskState) {
            RiskState.INCREASED_RISK -> numberOfDaysWithHighRisk
            RiskState.LOW_RISK -> numberOfDaysWithLowRisk
            else -> 0
        }

    val checkInOverlapCount: Int
        get() = checkInWarningOverlaps?.size ?: 0
}
