package de.rki.coronawarnapp.presencetracing.risk.storage

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningOverlap
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.risk.RiskState
import kotlinx.coroutines.flow.Flow
import org.joda.time.Instant

/*
* Stores matches from the last successful execution
* */
@Dao
interface TraceTimeIntervalMatchDao {

    @Query("SELECT * FROM TraceTimeIntervalMatchEntity")
    fun allMatches(): Flow<List<TraceTimeIntervalMatchEntity>>

    @Query("DELETE FROM TraceTimeIntervalMatchEntity")
    suspend fun deleteAll()

    @Query("DELETE FROM TraceTimeIntervalMatchEntity WHERE endTimeMillis < :endTimeMillis")
    suspend fun deleteOlderThan(endTimeMillis: Long)

    @Query("DELETE FROM TraceTimeIntervalMatchEntity WHERE traceWarningPackageId = :warningPackageId")
    suspend fun deleteMatchesForPackage(warningPackageId: String)

    @Insert
    suspend fun insert(entities: List<TraceTimeIntervalMatchEntity>)
}

@Entity
data class TraceTimeIntervalMatchEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long? = null,
    @ColumnInfo(name = "checkInId") val checkInId: Long,
    @ColumnInfo(name = "traceWarningPackageId") val traceWarningPackageId: String,
    @ColumnInfo(name = "transmissionRiskLevel") val transmissionRiskLevel: Int,
    @ColumnInfo(name = "startTimeMillis") val startTimeMillis: Long,
    @ColumnInfo(name = "endTimeMillis") val endTimeMillis: Long
)

internal fun CheckInWarningOverlap.toTraceTimeIntervalMatchEntity() = TraceTimeIntervalMatchEntity(
    checkInId = checkInId,
    traceWarningPackageId = traceWarningPackageId,
    transmissionRiskLevel = transmissionRiskLevel,
    startTimeMillis = startTime.millis,
    endTimeMillis = endTime.millis
)

internal fun TraceTimeIntervalMatchEntity.toCheckInWarningOverlap() = CheckInWarningOverlap(
    checkInId = checkInId,
    traceWarningPackageId = traceWarningPackageId,
    transmissionRiskLevel = transmissionRiskLevel,
    startTime = Instant.ofEpochMilli(startTimeMillis),
    endTime = Instant.ofEpochMilli(endTimeMillis)
)

@Suppress("MaxLineLength")
@Dao
interface PresenceTracingRiskLevelResultDao {

    @Query("SELECT * FROM PresenceTracingRiskLevelResultEntity ORDER BY calculatedAtMillis DESC LIMIT :limit")
    fun latestEntries(limit: Int): Flow<List<PresenceTracingRiskLevelResultEntity>>

    @Query("SELECT * FROM PresenceTracingRiskLevelResultEntity")
    fun allEntries(): Flow<List<PresenceTracingRiskLevelResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: PresenceTracingRiskLevelResultEntity)

    @Query("DELETE FROM PresenceTracingRiskLevelResultEntity WHERE calculatedAtMillis < :calculatedAtMillis")
    suspend fun deleteOlderThan(calculatedAtMillis: Long)

    @Query("DELETE FROM PresenceTracingRiskLevelResultEntity")
    suspend fun deleteAll()
}

@Entity
data class PresenceTracingRiskLevelResultEntity(
    @PrimaryKey @ColumnInfo(name = "calculatedAtMillis") val calculatedAtMillis: Long,
    @ColumnInfo(name = "riskStateCode") val riskState: RiskState,
    @ColumnInfo(name = "calculatedFromMillis") val calculatedFromMillis: Long,
)

internal fun PresenceTracingRiskLevelResultEntity.toRiskLevelResult(
    presenceTracingDayRisks: List<PresenceTracingDayRisk>? = null,
    traceLocationCheckInRiskStates: List<TraceLocationCheckInRisk>? = null,
    checkInWarningOverlaps: List<CheckInWarningOverlap>? = null
) = PtRiskLevelResult(
    calculatedAt = Instant.ofEpochMilli((calculatedAtMillis)),
    riskState = riskState,
    presenceTracingDayRisk = presenceTracingDayRisks,
    traceLocationCheckInRiskStates = traceLocationCheckInRiskStates,
    checkInWarningOverlaps = checkInWarningOverlaps,
    calculatedFrom = Instant.ofEpochMilli((calculatedFromMillis)),
)

internal fun PtRiskLevelResult.toRiskLevelEntity() = PresenceTracingRiskLevelResultEntity(
    calculatedAtMillis = calculatedAt.millis,
    riskState = riskState,
    calculatedFromMillis = calculatedFrom.millis,
)

class RiskStateConverter {
    @TypeConverter
    fun toRiskStateCode(value: Int?): RiskState? = value?.toRiskState()

    @TypeConverter
    fun fromRiskStateCode(code: RiskState?): Int? = code?.toCode()

    private fun RiskState.toCode() = when (this) {
        RiskState.CALCULATION_FAILED -> CALCULATION_FAILED
        RiskState.LOW_RISK -> LOW_RISK
        RiskState.INCREASED_RISK -> INCREASED_RISK
    }

    private fun Int.toRiskState() = when (this) {
        CALCULATION_FAILED -> RiskState.CALCULATION_FAILED
        LOW_RISK -> RiskState.LOW_RISK
        INCREASED_RISK -> RiskState.INCREASED_RISK
        else -> null
    }

    companion object {
        private const val CALCULATION_FAILED = 0
        private const val LOW_RISK = 1
        private const val INCREASED_RISK = 2
    }
}
