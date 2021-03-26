package de.rki.coronawarnapp.presencetracing.warning.riskcalculation

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel

// converts number of 10min intervals into milliseconds
internal fun Int.tenMinIntervalToMillis() = this * MILLIS_IN_MIN

private const val MILLIS_IN_MIN = 600L * 1000L

fun RiskLevel.mapToRiskState(): RiskState {
    return when (this) {
        RiskLevel.UNSPECIFIED, RiskLevel.UNRECOGNIZED -> RiskState.CALCULATION_FAILED
        RiskLevel.LOW -> RiskState.LOW_RISK
        RiskLevel.HIGH -> RiskState.INCREASED_RISK
    }
}
