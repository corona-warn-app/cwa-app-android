package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.util.toLocalDateUtc
import java.time.Instant
import java.time.LocalDate

data class ExposureWindowDayRisk(
    val dateMillisSinceEpoch: Long,
    val riskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel,
    val minimumDistinctEncountersWithLowRisk: Int,
    val minimumDistinctEncountersWithHighRisk: Int
) {
    val localDateUtc: LocalDate = Instant.ofEpochMilli(dateMillisSinceEpoch).toLocalDateUtc()
}
