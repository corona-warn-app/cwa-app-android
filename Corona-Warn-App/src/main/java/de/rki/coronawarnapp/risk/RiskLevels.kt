package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.appconfig.ConfigData
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

    fun isIncreasedRisk(appConfig: ConfigData, exposureWindows: List<ExposureWindow>): Boolean

    fun updateRepository(
        riskLevel: RiskLevel,
        time: Long
    )

    fun calculateRisk(
        appConfig: ConfigData,
        exposureWindow: ExposureWindow
    ): RiskResult?

    fun aggregateResults(
        appConfig: ConfigData,
        exposureWindowsAndResult: Map<ExposureWindow, RiskResult>
    ): AggregatedRiskResult
}
