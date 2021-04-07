package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel
import java.util.concurrent.TimeUnit

// converts number of 10min intervals into milliseconds
internal fun Int.tenMinIntervalToMillis() = this * TimeUnit.MINUTES.toMillis(10L)

fun RiskLevel.mapToRiskState(): RiskState {
    return when (this) {
        RiskLevel.UNSPECIFIED, RiskLevel.UNRECOGNIZED -> RiskState.CALCULATION_FAILED
        RiskLevel.LOW -> RiskState.LOW_RISK
        RiskLevel.HIGH -> RiskState.INCREASED_RISK
    }
}
