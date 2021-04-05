package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel

// converts number of 10min intervals into milliseconds
internal fun Int.tenMinIntervalToMillis() = this * MILLIS_IN_TEN_MIN

private const val MILLIS_IN_TEN_MIN = 600000L

fun RiskLevel.mapToRiskState(): RiskState {
    return when (this) {
        RiskLevel.UNSPECIFIED, RiskLevel.UNRECOGNIZED -> RiskState.CALCULATION_FAILED
        RiskLevel.LOW -> RiskState.LOW_RISK
        RiskLevel.HIGH -> RiskState.INCREASED_RISK
    }
}
