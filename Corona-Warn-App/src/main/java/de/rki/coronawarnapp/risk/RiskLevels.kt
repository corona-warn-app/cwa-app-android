package de.rki.coronawarnapp.risk

interface RiskLevels {

    val calculationNotPossibleBecauseNoKeys: Boolean

    val calculationNotPossibleBecauseOfOutdatedResults: Boolean

    /**
     * true if threshold is reached / if the duration of the activated tracing time is above the
     * defined value
     */
    val isActiveTracingTimeAboveThreshold: Boolean

    suspend fun isIncreasedRisk(): Boolean

    suspend fun calculationNotPossibleBecauseTracingIsOff(): Boolean

    fun updateRepository(
        riskLevel: RiskLevel,
        time: Long
    )
}
