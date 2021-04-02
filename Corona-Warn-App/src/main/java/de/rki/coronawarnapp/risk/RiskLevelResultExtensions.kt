package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.CombinedEwPtRiskLevelResult
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

fun List<CombinedEwPtRiskLevelResult>.tryLatestResultsWithDefaults(): DisplayableRiskResults {
    val latestCalculation = this.maxByOrNull { it.calculatedAt }
        ?: initialLowLevelEwRiskLevelResult

    val lastSuccessfullyCalculated = this.filter { it.wasSuccessfullyCalculated }
        .maxByOrNull { it.calculatedAt } ?: undeterminedEwRiskLevelResult

    return DisplayableRiskResults(
        lastCalculated = latestCalculation,
        lastSuccessfullyCalculated = lastSuccessfullyCalculated
    )
}

data class DisplayableRiskResults(
    val lastCalculated: CombinedEwPtRiskLevelResult,
    val lastSuccessfullyCalculated: CombinedEwPtRiskLevelResult
)

private val undeterminedEwRiskLevelResult = CombinedEwPtRiskLevelResult(
    PtRiskLevelResult(
        calculatedAt = Instant.EPOCH,
        riskState = RiskState.CALCULATION_FAILED
    ),
    EwUndeterminedRiskLevelResult
)

private val initialLowLevelEwRiskLevelResult = CombinedEwPtRiskLevelResult(
    PtRiskLevelResult(
        calculatedAt = Instant.now(),
        riskState = RiskState.LOW_RISK
    ),
    EwInitialLowRiskLevelResult
)

private object EwInitialLowRiskLevelResult : EwRiskLevelResult {
    override val calculatedAt: Instant = Instant.now()
    override val riskState: RiskState = RiskState.LOW_RISK
    override val failureReason: EwRiskLevelResult.FailureReason? = null
    override val ewAggregatedRiskResult: EwAggregatedRiskResult? = null
    override val exposureWindows: List<ExposureWindow>? = null
    override val matchedKeyCount: Int = 0
    override val daysWithEncounters: Int = 0
}

private object EwUndeterminedRiskLevelResult : EwRiskLevelResult {
    override val calculatedAt: Instant = Instant.EPOCH
    override val riskState: RiskState = RiskState.CALCULATION_FAILED
    override val failureReason: EwRiskLevelResult.FailureReason? = null
    override val ewAggregatedRiskResult: EwAggregatedRiskResult? = null
    override val exposureWindows: List<ExposureWindow>? = null
    override val matchedKeyCount: Int = 0
    override val daysWithEncounters: Int = 0
}
