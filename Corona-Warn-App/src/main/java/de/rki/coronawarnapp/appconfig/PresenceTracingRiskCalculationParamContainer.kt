package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.TransmissionRiskValueMapping

data class PresenceTracingRiskCalculationParamContainer(
    val transmissionRiskValueMapping: List<TransmissionRiskValueMapping> = emptyList(),
    val normalizedTimePerCheckInToRiskLevelMapping: List<NormalizedTimeToRiskLevelMapping> = emptyList(),
    val normalizedTimePerDayToRiskLevelMapping: List<NormalizedTimeToRiskLevelMapping> = emptyList(),
    val maxCheckInAgeInDays: Int = 14 // default to previous duration
)
