package de.rki.coronawarnapp.ui.tracing.common

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelResult
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
    override val riskLevel: RiskLevel = RiskLevel.LOW_LEVEL_RISK
    override val failureReason: RiskLevelResult.FailureReason? = null
    override val aggregatedRiskResult: AggregatedRiskResult? = null
    override val exposureWindows: List<ExposureWindow>? = null
    override val matchedKeyCount: Int = 0
    override val daysWithEncounters: Int = 0
}

private object UndeterminedRiskLevelResult : RiskLevelResult {
    override val calculatedAt: Instant = Instant.EPOCH
    override val riskLevel: RiskLevel = RiskLevel.UNDETERMINED
    override val failureReason: RiskLevelResult.FailureReason? = null
    override val aggregatedRiskResult: AggregatedRiskResult? = null
    override val exposureWindows: List<ExposureWindow>? = null
    override val matchedKeyCount: Int = 0
    override val daysWithEncounters: Int = 0
}
