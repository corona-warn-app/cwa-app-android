package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import org.joda.time.Instant

interface RiskLevelResult {
    val calculatedAt: Instant

    val riskLevel: RiskLevel
        get() = when {
            aggregatedRiskResult?.isIncreasedRisk() == true -> RiskLevel.INCREASED_RISK
            aggregatedRiskResult?.isLowRisk() == true -> RiskLevel.LOW_LEVEL_RISK
            failureReason == FailureReason.OUTDATED_RESULTS -> RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
            failureReason == FailureReason.OUTDATED_RESULTS_MANUAL -> RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
            failureReason == FailureReason.TRACING_OFF -> RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
            failureReason == FailureReason.NO_INTERNET -> RiskLevel.UNKNOWN_RISK_NO_INTERNET
            failureReason == FailureReason.UNKNOWN -> RiskLevel.UNDETERMINED
            else -> RiskLevel.UNDETERMINED
        }

    val failureReason: FailureReason?
    val aggregatedRiskResult: AggregatedRiskResult?

    /**
     * This will only be filled in deviceForTester builds
     */
    val exposureWindows: List<ExposureWindow>?

    val wasSuccessfullyCalculated: Boolean
        get() = aggregatedRiskResult != null

    val isIncreasedRisk: Boolean
        get() = aggregatedRiskResult?.isIncreasedRisk() ?: false

    val matchedKeyCount: Int
        get() = if (isIncreasedRisk) {
            aggregatedRiskResult?.totalMinimumDistinctEncountersWithHighRisk ?: 0
        } else {
            aggregatedRiskResult?.totalMinimumDistinctEncountersWithLowRisk ?: 0
        }

    val daysWithEncounters: Int
        get() = if (isIncreasedRisk) {
            aggregatedRiskResult?.numberOfDaysWithHighRisk ?: 0
        } else {
            aggregatedRiskResult?.numberOfDaysWithLowRisk ?: 0
        }

    val lastRiskEncounterAt: Instant?
        get() = if (isIncreasedRisk) {
            aggregatedRiskResult?.mostRecentDateWithHighRisk
        } else {
            aggregatedRiskResult?.mostRecentDateWithLowRisk
        }

    enum class FailureReason(val failureCode: String) {
        UNKNOWN("unknown"),
        TRACING_OFF("tracingOff"),
        NO_INTERNET("noInternet"),
        OUTDATED_RESULTS("outDatedResults"),
        OUTDATED_RESULTS_MANUAL("outDatedResults.manual")
    }

    companion object {
        private val UNSUCCESSFUL_RISK_LEVELS = arrayOf(
            RiskLevel.UNDETERMINED,
            RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS,
            RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
        )
    }
}
