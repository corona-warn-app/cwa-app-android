package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import org.joda.time.Instant

fun List<RiskLevelResult>.tryLatestResultsWithDefaults(): DisplayableRiskResults {
    val latestCalculation = this.maxByOrNull { it.calculatedAt }
        ?: InitialLowLevelRiskLevelResult

    val lastSuccessfullyCalculated = this.filter { it.wasSuccessfullyCalculated }
        .maxByOrNull { it.calculatedAt } ?: UndeterminedRiskLevelResult

    return DisplayableRiskResults(
        lastCalculated = latestCalculation,
        lastSuccessfullyCalculated = lastSuccessfullyCalculated
    )
}

data class DisplayableRiskResults(
    val lastCalculated: RiskLevelResult,
    val lastSuccessfullyCalculated: RiskLevelResult
)

private object InitialLowLevelRiskLevelResult : RiskLevelResult {
    override val calculatedAt: Instant = Instant.now()
    override val riskState: RiskState = RiskState.LOW_RISK
    override val failureReason: RiskLevelResult.FailureReason? = null
    override val aggregatedRiskResult: AggregatedRiskResult? = null
    override val exposureWindows: List<ExposureWindow>? = null
    override val matchedKeyCount: Int = 0
    override val daysWithEncounters: Int = 0
}

private object UndeterminedRiskLevelResult : RiskLevelResult {
    override val calculatedAt: Instant = Instant.EPOCH
    override val riskState: RiskState = RiskState.CALCULATION_FAILED
    override val failureReason: RiskLevelResult.FailureReason? = null
    override val aggregatedRiskResult: AggregatedRiskResult? = null
    override val exposureWindows: List<ExposureWindow>? = null
    override val matchedKeyCount: Int = 0
    override val daysWithEncounters: Int = 0
}
