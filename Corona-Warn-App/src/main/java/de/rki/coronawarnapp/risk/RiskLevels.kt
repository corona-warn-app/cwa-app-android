package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.result.RiskResult
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

interface RiskLevels {

    fun calculationNotPossibleBecauseOfNoKeys(): Boolean

    fun calculationNotPossibleBecauseOfOutdatedResults(): Boolean

    /**
     * true if threshold is reached / if the duration of the activated tracing time is above the
     * defined value
     */
    fun isActiveTracingTimeAboveThreshold(): Boolean

    suspend fun isIncreasedRisk(lastExposureSummary: ExposureSummary): Boolean

    fun updateRepository(
        riskLevel: RiskLevel,
        time: Long
    )

    @Deprecated("Switch to new calculation with Exposure Window")
    fun calculateRiskScore(
        attenuationParameters: AttenuationDurationOuterClass.AttenuationDuration,
        exposureSummary: ExposureSummary
    ): Double

    suspend fun calculateRisk(
        exposureWindow: ExposureWindow
    ): RiskResult?

    fun aggregateResults(
        exposureWindowsAndResult: Map<ExposureWindow, RiskResult>,
        riskCalculationParameters: RiskCalculationParametersOuterClass.RiskCalculationParameters
    ): AggregatedRiskResult
}
