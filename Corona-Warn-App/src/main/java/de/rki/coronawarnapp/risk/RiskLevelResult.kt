package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import org.joda.time.Instant

interface RiskLevelResult {
    val riskLevel: RiskLevel
    val calculatedAt: Instant

    val aggregatedRiskResult: AggregatedRiskResult?

    /**
     * This will only be filled in deviceForTester builds
     */
    val exposureWindows: List<ExposureWindow>?

    val wasSuccessfullyCalculated: Boolean
        get() = !UNSUCCESSFUL_RISK_LEVELS.contains(riskLevel)

    val isIncreasedRisk: Boolean
        get() = aggregatedRiskResult?.isHighRisk() ?: false

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

    companion object {
        private val UNSUCCESSFUL_RISK_LEVELS = arrayOf(
            RiskLevel.UNDETERMINED,
            RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS,
            RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
        )
    }
}
