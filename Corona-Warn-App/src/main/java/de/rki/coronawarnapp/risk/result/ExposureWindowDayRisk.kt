package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import java.time.Instant

data class ExposureWindowDayRisk(
    val dateMillisSinceEpoch: Long,
    val riskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel,
    val minimumDistinctEncountersWithLowRisk: Int,
    val minimumDistinctEncountersWithHighRisk: Int
) {
    val localDateUtc = Instant.ofEpochMilli(dateMillisSinceEpoch).toLocalDateUtc()
}
