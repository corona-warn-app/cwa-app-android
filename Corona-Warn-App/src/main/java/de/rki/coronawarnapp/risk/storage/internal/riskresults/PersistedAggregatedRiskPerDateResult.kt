package de.rki.coronawarnapp.risk.storage.internal.riskresults

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass

@Suppress("MaxLineLength")
@Entity(tableName = "riskperdate")
data class PersistedAggregatedRiskPerDateResult(
    @PrimaryKey @ColumnInfo(name = "dateMillisSinceEpoch") val dateMillisSinceEpoch: Long,
    @ColumnInfo(name = "riskLevel") val riskLevel: RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel,
    @ColumnInfo(name = "minimumDistinctEncountersWithLowRisk") val minimumDistinctEncountersWithLowRisk: Int,
    @ColumnInfo(name = "minimumDistinctEncountersWithHighRisk") val minimumDistinctEncountersWithHighRisk: Int
) {
    fun toAggregatedRiskPerDateResult(): AggregatedRiskPerDateResult =
        AggregatedRiskPerDateResult(
            dateMillisSinceEpoch = dateMillisSinceEpoch,
            riskLevel = riskLevel,
            minimumDistinctEncountersWithLowRisk = minimumDistinctEncountersWithLowRisk,
            minimumDistinctEncountersWithHighRisk = minimumDistinctEncountersWithHighRisk
        )
}

fun AggregatedRiskPerDateResult.toPersistedAggregatedRiskPerDateResult(): PersistedAggregatedRiskPerDateResult =
    PersistedAggregatedRiskPerDateResult(
        dateMillisSinceEpoch = dateMillisSinceEpoch,
        riskLevel = riskLevel,
        minimumDistinctEncountersWithLowRisk = minimumDistinctEncountersWithLowRisk,
        minimumDistinctEncountersWithHighRisk = minimumDistinctEncountersWithHighRisk
    )
