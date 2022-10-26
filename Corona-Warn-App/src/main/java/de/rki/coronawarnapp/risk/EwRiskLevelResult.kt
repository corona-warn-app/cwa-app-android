package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.EwAggregatedRiskResult
import java.time.Instant

interface EwRiskLevelResult {
    val calculatedAt: Instant

    val riskState: RiskState
        get() = when {
            ewAggregatedRiskResult?.isIncreasedRisk() == true -> RiskState.INCREASED_RISK
            ewAggregatedRiskResult?.isLowRisk() == true -> RiskState.LOW_RISK
            else -> RiskState.CALCULATION_FAILED
        }

    val failureReason: FailureReason?
    val ewAggregatedRiskResult: EwAggregatedRiskResult?

    /**
     * This will only be filled in deviceForTester builds
     */
    val exposureWindows: List<ExposureWindow>?

    val wasSuccessfullyCalculated: Boolean
        get() = ewAggregatedRiskResult != null

    val isIncreasedRisk: Boolean
        get() = ewAggregatedRiskResult?.isIncreasedRisk() ?: false

    val matchedKeyCount: Int
        get() = if (isIncreasedRisk) {
            ewAggregatedRiskResult?.totalMinimumDistinctEncountersWithHighRisk ?: 0
        } else {
            ewAggregatedRiskResult?.totalMinimumDistinctEncountersWithLowRisk ?: 0
        }

    val mostRecentDateAtHighRisk
        get() = ewAggregatedRiskResult?.mostRecentDateWithHighRisk

    val mostRecentDateAtLowRisk
        get() = ewAggregatedRiskResult?.mostRecentDateWithLowRisk

    val mostRecentDateAtRiskState: Instant?
        get() = if (isIncreasedRisk) {
            ewAggregatedRiskResult?.mostRecentDateWithHighRisk
        } else {
            ewAggregatedRiskResult?.mostRecentDateWithLowRisk
        }

    enum class FailureReason(val failureCode: String) {
        UNKNOWN("unknown"),
        TRACING_OFF("tracingOff"),
        NO_INTERNET("noInternet"),
        INCORRECT_DEVICE_TIME("incorrectDeviceTime"),
        OUTDATED_RESULTS("outDatedResults"),
        OUTDATED_RESULTS_MANUAL("outDatedResults.manual"),
        POSITIVE_TEST_RESULT("positiveTestResult")
    }
}
