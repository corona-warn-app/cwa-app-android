package de.rki.coronawarnapp.risk

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.risk.storage.internal.RiskCombinator
import de.rki.coronawarnapp.util.toLocalDateUtc
import java.time.Instant
import java.time.LocalDate

data class CombinedEwPtDayRisk(
    val localDate: LocalDate,
    val riskState: RiskState
)

data class CombinedEwPtRiskLevelResult(
    val ptRiskLevelResult: PtRiskLevelResult,
    val ewRiskLevelResult: EwRiskLevelResult,
    private val exposureWindowDayRisks: List<ExposureWindowDayRisk>? = null
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
            RiskState.INCREASED_RISK ->
                ewDaysWithHighRisk
                    .plus(ptRiskLevelResult.daysWithHighRisk)
                    .distinct()
                    .count()

            RiskState.LOW_RISK ->
                ewDaysWithLowRisk
                    .plus(ptRiskLevelResult.daysWithLowRisk)
                    .distinct()
                    .count()

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

    @VisibleForTesting
    internal val ewDaysWithHighRisk: List<LocalDate>
        get() = exposureWindowDayRisks?.filter {
            it.riskLevel.mapToRiskState() == RiskState.INCREASED_RISK
        }?.map { it.localDateUtc } ?: emptyList()

    @VisibleForTesting
    internal val ewDaysWithLowRisk: List<LocalDate>
        get() = exposureWindowDayRisks?.filter {
            it.riskLevel.mapToRiskState() == RiskState.LOW_RISK
        }?.map { it.localDateUtc } ?: emptyList()
}

data class LastCombinedRiskResults(
    val lastCalculated: CombinedEwPtRiskLevelResult,
    val lastSuccessfullyCalculatedRiskState: RiskState
)

data class LastSuccessfulRiskResult(
    val riskState: RiskState,
    val mostRecentDateAtRiskState: Instant?
)

internal fun max(left: Instant, right: Instant): Instant {
    return Instant.ofEpochMilli(kotlin.math.max(left.toEpochMilli(), right.toEpochMilli()))
}

internal fun max(left: LocalDate?, right: LocalDate?): LocalDate? {
    if (left == null) return right
    if (right == null) return left
    return if (left.isAfter(right)) left else right
}
