package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.storage.internal.RiskCombinator
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import org.joda.time.Instant
import org.joda.time.LocalDate

data class CombinedEwPtDayRisk(
    val localDate: LocalDate,
    val riskState: RiskState
)

data class CombinedEwPtRiskLevelResult(
    private val ptRiskLevelResult: PtRiskLevelResult,
    private val ewRiskLevelResult: EwRiskLevelResult
) {

    val riskState: RiskState by lazy {
        RiskCombinator.combine(ptRiskLevelResult.riskState, ewRiskLevelResult.riskState)
    }

    val wasSuccessfullyCalculated: Boolean by lazy {
        riskState != RiskState.CALCULATION_FAILED
    }

    val calculatedAt: Instant by lazy {
        max(ewRiskLevelResult.calculatedAt, ptRiskLevelResult.calculatedAt)
    }

    val daysWithEncounters: Int by lazy {
        when (riskState) {
            RiskState.INCREASED_RISK -> {
                ewRiskLevelResult.daysWithHighRisk
                    .plus(ptRiskLevelResult.daysWithHighRisk)
                    .distinct().count()
            }
            RiskState.LOW_RISK -> {
                ewRiskLevelResult.daysWithLowRisk
                    .plus(ptRiskLevelResult.daysWithLowRisk)
                    .distinct().count()
            }
            else -> 0
        }
    }

    val lastRiskEncounterAt: LocalDate? by lazy {
        when (riskState) {
            RiskState.INCREASED_RISK -> max(
                ewRiskLevelResult.ewAggregatedRiskResult?.mostRecentDateWithHighRisk?.toLocalDateUtc(),
                ptRiskLevelResult.mostRecentDateWithHighRisk
            )
            RiskState.LOW_RISK -> max(
                ewRiskLevelResult.ewAggregatedRiskResult?.mostRecentDateWithLowRisk?.toLocalDateUtc(),
                ptRiskLevelResult.mostRecentDateWithLowRisk
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
        ewRiskLevelResult.matchedKeyCount + ptRiskLevelResult.checkInOverlapCount
    }
}

data class LastCombinedRiskResults(
    val lastCalculated: CombinedEwPtRiskLevelResult,
    val lastSuccessfullyCalculated: CombinedEwPtRiskLevelResult
)

internal fun max(left: Instant, right: Instant): Instant {
    return Instant.ofEpochMilli(kotlin.math.max(left.millis, right.millis))
}

internal fun max(left: LocalDate?, right: LocalDate?): LocalDate? {
    if (left == null) return right
    if (right == null) return left
    return if (left.isAfter(right)) left else right
}
