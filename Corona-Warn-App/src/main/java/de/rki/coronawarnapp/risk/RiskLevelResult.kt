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

    val matchedKeyCount: Int
    val daysSinceLastExposure: Int

    companion object {
        private val UNSUCCESSFUL_RISK_LEVELS = arrayOf(
            RiskLevel.UNDETERMINED,
            RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF,
            RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS,
            RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
        )
    }
}
