package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.task.Task
import org.joda.time.Instant

data class RiskLevelTaskResult(
    override val riskLevel: RiskLevel,
    override val calculatedAt: Instant,
    override val aggregatedRiskResult: AggregatedRiskResult? = null,
    override val exposureWindows: List<ExposureWindow>? = null
) : Task.Result, RiskLevelResult {

    override val isIncreasedRisk: Boolean = aggregatedRiskResult?.isIncreasedRisk() ?: false

    override val matchedKeyCount: Int
        get() = if (isIncreasedRisk) {
            aggregatedRiskResult?.totalMinimumDistinctEncountersWithHighRisk ?: 0
        } else {
            aggregatedRiskResult?.totalMinimumDistinctEncountersWithLowRisk ?: 0
        }

    override val daysSinceLastExposure: Int
        get() = if (isIncreasedRisk) {
            aggregatedRiskResult?.numberOfDaysWithHighRisk ?: 0
        } else {
            aggregatedRiskResult?.numberOfDaysWithLowRisk ?: 0
        }
}
