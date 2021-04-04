package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import org.joda.time.Instant

fun List<EwRiskLevelResult>.tryLatestEwResultsWithDefaults(): DisplayableEwRiskResults {
    val latestCalculation = this.maxByOrNull { it.calculatedAt }
        ?: EwInitialLowRiskLevelResult

    val lastSuccessfullyCalculated = this.filter { it.wasSuccessfullyCalculated }
        .maxByOrNull { it.calculatedAt } ?: EwUndeterminedRiskLevelResult

    return DisplayableEwRiskResults(
        lastCalculated = latestCalculation,
        lastSuccessfullyCalculated = lastSuccessfullyCalculated
    )
}

data class DisplayableEwRiskResults(
    val lastCalculated: EwRiskLevelResult,
    val lastSuccessfullyCalculated: EwRiskLevelResult
)

object EwInitialLowRiskLevelResult : EwRiskLevelResult {
    override val calculatedAt: Instant = Instant.now()
    override val riskState: RiskState = RiskState.LOW_RISK
    override val failureReason: EwRiskLevelResult.FailureReason? = null
    override val ewAggregatedRiskResult: EwAggregatedRiskResult? = null
    override val exposureWindows: List<ExposureWindow>? = null
    override val matchedKeyCount: Int = 0
    override val daysWithEncounters: Int = 0
}

object EwUndeterminedRiskLevelResult : EwRiskLevelResult {
    override val calculatedAt: Instant = Instant.EPOCH
    override val riskState: RiskState = RiskState.CALCULATION_FAILED
    override val failureReason: EwRiskLevelResult.FailureReason? = null
    override val ewAggregatedRiskResult: EwAggregatedRiskResult? = null
    override val exposureWindows: List<ExposureWindow>? = null
    override val matchedKeyCount: Int = 0
    override val daysWithEncounters: Int = 0
}
