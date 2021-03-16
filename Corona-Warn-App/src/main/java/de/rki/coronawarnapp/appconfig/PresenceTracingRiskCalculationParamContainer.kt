package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.TransmissionRiskValueMapping

data class PresenceTracingRiskCalculationParamContainer(
    val transmissionRiskValueMapping: List<TransmissionRiskValueMapping>,
    val normalizedTimePerCheckInToRiskLevelMapping: List<NormalizedTimeToRiskLevelMapping>,
    val normalizedTimePerDayToRiskLevelMapping: List<NormalizedTimeToRiskLevelMapping>
)
