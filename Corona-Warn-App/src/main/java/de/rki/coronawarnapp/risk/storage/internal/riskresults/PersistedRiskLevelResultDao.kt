package de.rki.coronawarnapp.risk.storage.internal.riskresults

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelTaskResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping
import org.joda.time.Instant

@Entity(tableName = "riskresults")
data class PersistedRiskLevelResultDao(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "riskLevel") val riskLevel: RiskLevel,
    @ColumnInfo(name = "calculatedAt") val calculatedAt: Instant,
    @Embedded val aggregatedRiskResult: PersistedAggregatedRiskResult?
) {

    fun toRiskResult() = RiskLevelTaskResult(
        riskLevel = riskLevel,
        calculatedAt = calculatedAt,
        aggregatedRiskResult = aggregatedRiskResult?.toAggregatedRiskResult()
    )

    data class PersistedAggregatedRiskResult(
        @ColumnInfo(name = "totalRiskLevel")
        val totalRiskLevel: NormalizedTimeToRiskLevelMapping.RiskLevel,
        @ColumnInfo(name = "totalMinimumDistinctEncountersWithLowRisk")
        val totalMinimumDistinctEncountersWithLowRisk: Int,
        @ColumnInfo(name = "totalMinimumDistinctEncountersWithHighRisk")
        val totalMinimumDistinctEncountersWithHighRisk: Int,
        @ColumnInfo(name = "mostRecentDateWithLowRisk")
        val mostRecentDateWithLowRisk: Instant?,
        @ColumnInfo(name = "mostRecentDateWithHighRisk")
        val mostRecentDateWithHighRisk: Instant?,
        @ColumnInfo(name = "numberOfDaysWithLowRisk")
        val numberOfDaysWithLowRisk: Int,
        @ColumnInfo(name = "numberOfDaysWithHighRisk")
        val numberOfDaysWithHighRisk: Int
    ) {

        fun toAggregatedRiskResult() = AggregatedRiskResult(
            totalRiskLevel = totalRiskLevel,
            totalMinimumDistinctEncountersWithLowRisk = totalMinimumDistinctEncountersWithLowRisk,
            totalMinimumDistinctEncountersWithHighRisk = totalMinimumDistinctEncountersWithHighRisk,
            mostRecentDateWithLowRisk = mostRecentDateWithLowRisk,
            mostRecentDateWithHighRisk = mostRecentDateWithHighRisk,
            numberOfDaysWithLowRisk = numberOfDaysWithLowRisk,
            numberOfDaysWithHighRisk = numberOfDaysWithHighRisk
        )

        class Converter {
            @TypeConverter
            fun toType(value: Int?): NormalizedTimeToRiskLevelMapping.RiskLevel? =
                value?.let { NormalizedTimeToRiskLevelMapping.RiskLevel.forNumber(value) }

            @TypeConverter
            fun fromType(type: NormalizedTimeToRiskLevelMapping.RiskLevel?): Int? = type?.number
        }
    }

    class Converter {
        @TypeConverter
        fun toType(value: Int?): RiskLevel? =
            value?.let { RiskLevel.values().single { it.raw == value } }

        @TypeConverter
        fun fromType(type: RiskLevel?): Int? = type?.raw
    }
}
