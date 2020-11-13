package de.rki.coronawarnapp.risk

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.appconfig.RiskCalculationConfig
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass

interface RiskLevels {

    fun calculationNotPossibleBecauseOfNoKeys(): Boolean

    fun calculationNotPossibleBecauseOfOutdatedResults(): Boolean

    /**
     * true if threshold is reached / if the duration of the activated tracing time is above the
     * defined value
     */
    fun isActiveTracingTimeAboveThreshold(): Boolean

    suspend fun isIncreasedRisk(
        lastExposureSummary: ExposureSummary,
        appConfiguration: RiskCalculationConfig
    ): Boolean

    fun updateRepository(
        riskLevel: RiskLevel,
        time: Long
    )

    fun calculateRiskScore(
        attenuationParameters: AttenuationDurationOuterClass.AttenuationDuration,
        exposureSummary: ExposureSummary
    ): Double
}
