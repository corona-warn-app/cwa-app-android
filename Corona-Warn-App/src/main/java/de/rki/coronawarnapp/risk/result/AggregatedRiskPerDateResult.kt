package de.rki.coronawarnapp.risk.result

import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat

data class AggregatedRiskPerDateResult(
    val dateMillisSinceEpoch: Long,
    val riskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel,
    val minimumDistinctEncountersWithLowRisk: Int,
    val minimumDistinctEncountersWithHighRisk: Int
) {
    val day: LocalDate
        get() {
            val dateFormatter = ISODateTimeFormat.date()
            val dateString = Instant.ofEpochMilli(dateMillisSinceEpoch).toString(dateFormatter)
            return LocalDate.parse(dateString, dateFormatter)
        }
}
