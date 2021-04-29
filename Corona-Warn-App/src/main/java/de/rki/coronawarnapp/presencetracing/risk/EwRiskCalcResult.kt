package de.rki.coronawarnapp.presencetracing.risk

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.mapToRiskState
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import org.joda.time.Instant
import org.joda.time.LocalDate

data class EwRiskCalcResult(
    val calculatedAt: Instant,
    val riskState: RiskState,
    private val totalMinimumDistinctEncountersWithHighRisk: Int = 0,
    private val totalMinimumDistinctEncountersWithLowRisk: Int = 0,
    private val exposureWindowDayRisks: List<ExposureWindowDayRisk>? = null
) {

    val wasSuccessfullyCalculated: Boolean by lazy {
        riskState != RiskState.CALCULATION_FAILED
    }

    val mostRecentDateWithHighRisk: LocalDate? by lazy {
        exposureWindowDayRisks
            ?.filter { it.riskLevel.mapToRiskState() == RiskState.INCREASED_RISK }
            ?.maxByOrNull { it.localDateUtc }
            ?.localDateUtc
    }

    val mostRecentDateWithLowRisk: LocalDate? by lazy {
        exposureWindowDayRisks
            ?.filter { it.riskLevel.mapToRiskState() == RiskState.LOW_RISK }
            ?.maxByOrNull { it.localDateUtc }
            ?.localDateUtc
    }

    val matchedKeyCount: Int
        get() = if (riskState == RiskState.INCREASED_RISK) {
            totalMinimumDistinctEncountersWithHighRisk
        } else {
            totalMinimumDistinctEncountersWithLowRisk
        }

    @VisibleForTesting
    internal val daysWithHighRisk: List<LocalDate>
        get() = exposureWindowDayRisks?.filter {
            it.riskLevel.mapToRiskState() == RiskState.INCREASED_RISK
        }?.map { it.localDateUtc } ?: emptyList()

    @VisibleForTesting
    internal val daysWithLowRisk: List<LocalDate>
        get() = exposureWindowDayRisks?.filter {
            it.riskLevel.mapToRiskState() == RiskState.LOW_RISK
        }?.map { it.localDateUtc } ?: emptyList()
}

fun EwRiskLevelResult.toEwRiskCalcResult(ewDayRisk: List<ExposureWindowDayRisk>? = null) = EwRiskCalcResult(
    calculatedAt = calculatedAt,
    riskState = riskState,
    totalMinimumDistinctEncountersWithHighRisk = ewAggregatedRiskResult?.totalMinimumDistinctEncountersWithHighRisk
        ?: 0,
    totalMinimumDistinctEncountersWithLowRisk = ewAggregatedRiskResult?.totalMinimumDistinctEncountersWithLowRisk ?: 0,
    exposureWindowDayRisks = ewDayRisk
)
