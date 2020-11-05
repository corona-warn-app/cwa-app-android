package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import org.joda.time.Instant

data class ExposureData(
    val date: Instant,
    val riskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel,
    val minimumDistinctEncountersWithLowRisk: Int,
    val minimumDistinctEncountersWithHighRisk: Int
)
