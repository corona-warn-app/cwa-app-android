package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.result.RiskResult

interface RiskLevels {

    fun calculationNotPossibleBecauseOfNoKeys(): Boolean

    fun calculationNotPossibleBecauseOfOutdatedResults(): Boolean

    /**
     * true if threshold is reached / if the duration of the activated tracing time is above the
     * defined value
     */
    fun isActiveTracingTimeAboveThreshold(): Boolean

    fun isIncreasedRisk(exposureWindows: List<ExposureWindow>): Boolean

    fun updateRepository(
        riskLevel: RiskLevel,
        time: Long
    )

    fun calculateRisk(
        exposureWindow: ExposureWindow
    ): RiskResult?

    fun aggregateResults(
        exposureWindowsAndResult: Map<ExposureWindow, RiskResult>
    ): AggregatedRiskResult
}
