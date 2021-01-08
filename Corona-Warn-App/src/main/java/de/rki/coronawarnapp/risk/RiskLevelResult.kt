package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import org.joda.time.Instant

interface RiskLevelResult {
    val calculatedAt: Instant

    val riskState: RiskState
        get() = when {
            aggregatedRiskResult?.isIncreasedRisk() == true -> RiskState.INCREASED_RISK
            aggregatedRiskResult?.isLowRisk() == true -> RiskState.LOW_RISK
            else -> RiskState.CALCULATION_FAILED
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
        INCORRECT_DEVICE_TIME("incorrectDeviceTime"),
        OUTDATED_RESULTS("outDatedResults"),
        OUTDATED_RESULTS_MANUAL("outDatedResults.manual")
    }
}
