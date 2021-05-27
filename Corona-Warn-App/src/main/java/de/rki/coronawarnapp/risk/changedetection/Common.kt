package de.rki.coronawarnapp.risk.changedetection

import de.rki.coronawarnapp.risk.RiskState

internal fun RiskState.hasChangedFromHighToLow(other: RiskState) =
    this == RiskState.INCREASED_RISK && other == RiskState.LOW_RISK

internal fun RiskState.hasChangedFromLowToHigh(other: RiskState) =
    this == RiskState.LOW_RISK && other == RiskState.INCREASED_RISK
