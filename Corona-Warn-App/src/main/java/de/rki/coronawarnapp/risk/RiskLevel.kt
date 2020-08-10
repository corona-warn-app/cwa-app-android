package de.rki.coronawarnapp.risk

enum class RiskLevel(val raw: Int) {
    // mapped to: unknown risk - initial
    // the risk score is not yet calculated
    // This score is set if the application was freshly installed without running the tracing
    UNKNOWN_RISK_INITIAL(RiskLevelConstants.UNKNOWN_RISK_INITIAL),

    // mapped to: no calculation possible
    // the ExposureNotification Framework or Bluetooth is not active
    // This risk score level has the highest priority and can oversteer the other risk score levels.
    NO_CALCULATION_POSSIBLE_TRACING_OFF(RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF),

    // mapped to: low risk
    // the risk score is higher than the riskScoreMinimumThreshold and lower than the riskScoreLevelThreshold
    // and the timeActivateTracing is higher than notEnoughDataTimeRange
    LOW_LEVEL_RISK(RiskLevelConstants.LOW_LEVEL_RISK),

    // mapped to: increased risk
    // the risk score is higher than the riskScoreLevelThreshold
    // The notEnoughDataTimeRange must not be not considered.
    INCREASED_RISK(RiskLevelConstants.INCREASED_RISK),

    // mapped to: unknown risk - outdated results
    // This risk status is shown if timeSinceLastExposureCalculation > maxStaleExposureRiskRange
    // and background jobs are enabled
    UNKNOWN_RISK_OUTDATED_RESULTS(RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS),

    // mapped to: unknown risk - outdated results manual
    // This risk status is shown if timeSinceLastExposureCalculation > maxStaleExposureRiskRange
    // and background jobs are disabled
    UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL(RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL),

    // mapped to no UI state
    // this should never happen
    UNDETERMINED(RiskLevelConstants.UNDETERMINED);

    companion object {
        fun forValue(value: Int): RiskLevel {
            return when (value) {
                RiskLevelConstants.UNKNOWN_RISK_INITIAL -> UNKNOWN_RISK_INITIAL
                RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF -> NO_CALCULATION_POSSIBLE_TRACING_OFF
                RiskLevelConstants.LOW_LEVEL_RISK -> LOW_LEVEL_RISK
                RiskLevelConstants.INCREASED_RISK -> INCREASED_RISK
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> UNKNOWN_RISK_OUTDATED_RESULTS
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL -> UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
                else -> UNDETERMINED
            }
        }

        // risk level categories
        val UNSUCCESSFUL_RISK_LEVELS =
            arrayOf(
                UNDETERMINED,
                NO_CALCULATION_POSSIBLE_TRACING_OFF,
                UNKNOWN_RISK_OUTDATED_RESULTS,
                UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
            )
        private val HIGH_RISK_LEVELS = arrayOf(INCREASED_RISK)
        private val LOW_RISK_LEVELS = arrayOf(
            UNKNOWN_RISK_INITIAL,
            NO_CALCULATION_POSSIBLE_TRACING_OFF,
            LOW_LEVEL_RISK,
            UNKNOWN_RISK_OUTDATED_RESULTS,
            UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL,
            UNDETERMINED
        )

        /**
         * Checks if the RiskLevel has change from a high to low or from low to high
         *
         * @param previousRiskLevel previously persisted RiskLevel
         * @param currentRiskLevel newly calculated RiskLevel
         * @return
         */
        fun riskLevelChangedBetweenLowAndHigh(
            previousRiskLevel: RiskLevel,
            currentRiskLevel: RiskLevel
        ): Boolean {
            return HIGH_RISK_LEVELS.contains(previousRiskLevel) && LOW_RISK_LEVELS.contains(currentRiskLevel) ||
                    LOW_RISK_LEVELS.contains(previousRiskLevel) && HIGH_RISK_LEVELS.contains(currentRiskLevel)
        }
    }
}
