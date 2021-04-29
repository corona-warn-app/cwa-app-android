package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.presencetracing.risk.EwRiskCalcResult
import de.rki.coronawarnapp.presencetracing.risk.PtRiskCalcResult
import de.rki.coronawarnapp.risk.storage.internal.RiskCombinator
import org.joda.time.Instant
import org.joda.time.LocalDate

data class CombinedEwPtDayRisk(
    val localDate: LocalDate,
    val riskState: RiskState
)

data class CombinedEwPtRiskCalcResult(
    private val ptRiskCalcResult: PtRiskCalcResult,
    private val ewRiskCalcResult: EwRiskCalcResult,
) {

    val riskState: RiskState by lazy {
        RiskCombinator.combine(ptRiskCalcResult.riskState, ewRiskCalcResult.riskState)
    }

    val wasSuccessfullyCalculated: Boolean by lazy {
        riskState != RiskState.CALCULATION_FAILED
    }

    val calculatedAt: Instant by lazy {
        max(ewRiskCalcResult.calculatedAt, ptRiskCalcResult.calculatedAt)
    }

    val daysWithEncounters: Int by lazy {
        when (riskState) {
            RiskState.INCREASED_RISK -> {
                ewRiskCalcResult.daysWithHighRisk
                    .plus(ptRiskCalcResult.daysWithHighRisk)
                    .distinct().count()
            }
            RiskState.LOW_RISK -> {
                ewRiskCalcResult.daysWithLowRisk
                    .plus(ptRiskCalcResult.daysWithLowRisk)
                    .distinct().count()
            }
            else -> 0
        }
    }

    val lastRiskEncounterAt: LocalDate? by lazy {
        when (riskState) {
            RiskState.INCREASED_RISK -> max(
                ewRiskCalcResult.mostRecentDateWithHighRisk,
                ptRiskCalcResult.mostRecentDateWithHighRisk
            )
            RiskState.LOW_RISK -> max(
                ewRiskCalcResult.mostRecentDateWithLowRisk,
                ptRiskCalcResult.mostRecentDateWithLowRisk
            )
            else -> null
        }
    }

    /**
     * The combination of matched exposure windows and overlaps.
     * If we have matches > 0, but are still in a low risk state,
     * the UI displays additional information in the risk details screen.
     */
    val matchedRiskCount: Int by lazy {
        ewRiskCalcResult.matchedKeyCount + ptRiskCalcResult.checkInOverlapCount
    }
}

data class LastCombinedRiskResults(
    val lastCalculated: CombinedEwPtRiskCalcResult,
    val lastSuccessfullyCalculatedRiskState: RiskState
)

internal fun max(left: Instant, right: Instant): Instant {
    return Instant.ofEpochMilli(kotlin.math.max(left.millis, right.millis))
}

internal fun max(left: LocalDate?, right: LocalDate?): LocalDate? {
    if (left == null) return right
    if (right == null) return left
    return if (left.isAfter(right)) left else right
}
