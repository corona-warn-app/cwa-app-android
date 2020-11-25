package de.rki.coronawarnapp.risk

enum class RiskLevel(val raw: Int) {
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
                RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF -> NO_CALCULATION_POSSIBLE_TRACING_OFF
                RiskLevelConstants.LOW_LEVEL_RISK -> LOW_LEVEL_RISK
                RiskLevelConstants.INCREASED_RISK -> INCREASED_RISK
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> UNKNOWN_RISK_OUTDATED_RESULTS
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL -> UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
                else -> UNDETERMINED
            }
        }
    }
}
