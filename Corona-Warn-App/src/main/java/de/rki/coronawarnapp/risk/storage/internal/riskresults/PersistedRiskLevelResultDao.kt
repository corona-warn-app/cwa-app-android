package de.rki.coronawarnapp.risk.storage.internal.riskresults

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import de.rki.coronawarnapp.risk.RiskLevelResult.FailureReason
import de.rki.coronawarnapp.risk.RiskLevelTaskResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDaoWrapper
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping
import org.joda.time.Instant
import timber.log.Timber

@Entity(tableName = "riskresults")
data class PersistedRiskLevelResultDao(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "monotonicId") val monotonicId: Long = 0,
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "calculatedAt") val calculatedAt: Instant,
    @ColumnInfo(name = "failureReason") val failureReason: FailureReason?,
    @Embedded val aggregatedRiskResult: PersistedAggregatedRiskResult?
) {

    fun toRiskResult(exposureWindows: List<PersistedExposureWindowDaoWrapper>? = null) = when {
        aggregatedRiskResult != null -> {
            RiskLevelTaskResult(
                calculatedAt = calculatedAt,
                aggregatedRiskResult = aggregatedRiskResult.toAggregatedRiskResult(),
                exposureWindows = exposureWindows?.map { it.toExposureWindow() }
            )
        }
        else -> {
            if (failureReason == null) {
                Timber.e("Entry contained no aggregateResult and no failure reason, shouldn't happen.")
            }
            RiskLevelTaskResult(
                calculatedAt = calculatedAt,
                failureReason = failureReason ?: FailureReason.UNKNOWN
            )
        }
    }

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
            fun toType(value: Int?): NormalizedTimeToRiskLevelMapping.RiskLevel? = value?.let {
                NormalizedTimeToRiskLevelMapping.RiskLevel.forNumber(value)
            }

            @TypeConverter
            fun fromType(type: NormalizedTimeToRiskLevelMapping.RiskLevel?): Int? = type?.number
        }
    }

    class Converter {
        @TypeConverter
        fun toType(value: String?): FailureReason? = value?.let {
            FailureReason.values().singleOrNull { it.failureCode == value }
        }

        @TypeConverter
        fun fromType(type: FailureReason?): String? = type?.failureCode
    }
}
