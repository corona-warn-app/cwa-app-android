package de.rki.coronawarnapp.presencetracing.risk

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.joda.time.Days
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceTracingRiskRepository @Inject constructor(
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator,
    private val databaseFactory: PresenceTracingRiskDatabase.Factory,
    private val timeStamper: TimeStamper
) {

    private val database by lazy {
        databaseFactory.create()
    }

    private val traceTimeIntervalMatchDao by lazy {
        database.traceTimeIntervalMatchDao()
    }

    private val riskLevelResultDao by lazy {
        database.presenceTracingRiskLevelResultDao()
    }

    private val allMatches = traceTimeIntervalMatchDao.allMatches().map { list ->
        list.map {
            it.toModel()
        }
    }

    private val normalizedTime = allMatches.map {
        presenceTracingRiskCalculator.calculateNormalizedTime(it)
    }

    val traceLocationCheckInRiskStates: Flow<List<TraceLocationCheckInRisk>> =
        normalizedTime.map {
            presenceTracingRiskCalculator.calculateCheckInRiskPerDay(it)
        }

    val presenceTracingDayRisk: Flow<List<PresenceTracingDayRisk>> =
        normalizedTime.map {
            presenceTracingRiskCalculator.calculateAggregatedRiskPerDay(it)
        }

    internal suspend fun reportSuccessfulCalculation(list: List<CheckInWarningOverlap>) {
        traceTimeIntervalMatchDao.insert(list.map { it.toEntity() })
        val fifteenDaysAgo = timeStamper.nowUTC.minus(Days.days(15).toStandardDuration()).toLocalDateUtc()
        val last14days = normalizedTime.first().filter { it.localDateUtc.isAfter(fifteenDaysAgo) }
        val risk = presenceTracingRiskCalculator.calculateTotalRisk(last14days)
        add(
            PtRiskLevelResult(
                timeStamper.nowUTC,
                risk
            )
        )
    }

    internal suspend fun deleteStaleData() {
        val fifteenDaysAgo = timeStamper.nowUTC.minus(Days.days(15).toStandardDuration())
        traceTimeIntervalMatchDao.deleteOlderThan(fifteenDaysAgo.millis)
        riskLevelResultDao.deleteOlderThan(fifteenDaysAgo.millis)
    }

    internal suspend fun deleteMatchesOfPackage(warningPackageId: String) {
        traceTimeIntervalMatchDao.deleteMatchesForPackage(warningPackageId)
    }

    suspend fun deleteAllMatches() {
        traceTimeIntervalMatchDao.deleteAll()
    }

    fun latestAndLastSuccessful() = riskLevelResultDao.latestAndLastSuccessful().map { it.map { it.toModel() } }

    fun latestEntries(limit: Int) = riskLevelResultDao.latestEntries(limit).map { it.map { it.toModel() } }

    fun add(riskLevelResult: PtRiskLevelResult) {
        riskLevelResultDao.insert(riskLevelResult.toEntity())
    }

    fun reportFailedCalculation() {
        add(
            PtRiskLevelResult(
                timeStamper.nowUTC,
                RiskState.CALCULATION_FAILED
            )
        )
    }
}

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
    @ForeignKey(
        entity = TraceLocationCheckInEntity::class,
        parentColumns = ["id"],
        childColumns = ["checkInId"],
        onDelete = ForeignKey.CASCADE
    )
    @ColumnInfo(name = "checkInId") val checkInId: Long,
    @ColumnInfo(name = "traceWarningPackageId") val traceWarningPackageId: String,
    @ColumnInfo(name = "transmissionRiskLevel") val transmissionRiskLevel: Int,
    @ColumnInfo(name = "startTimeMillis") val startTimeMillis: Long,
    @ColumnInfo(name = "endTimeMillis") val endTimeMillis: Long
)

private fun CheckInWarningOverlap.toEntity() = TraceTimeIntervalMatchEntity(
    checkInId = checkInId,
    traceWarningPackageId = traceWarningPackageId,
    transmissionRiskLevel = transmissionRiskLevel,
    startTimeMillis = startTime.millis,
    endTimeMillis = endTime.millis
)

private fun TraceTimeIntervalMatchEntity.toModel() = CheckInWarningOverlap(
    checkInId = checkInId,
    traceWarningPackageId = traceWarningPackageId,
    transmissionRiskLevel = transmissionRiskLevel,
    startTime = Instant.ofEpochMilli(startTimeMillis),
    endTime = Instant.ofEpochMilli(endTimeMillis)
)

@Dao
interface PresenceTracingRiskLevelResultDao {
    @Query("SELECT * FROM (SELECT * FROM PresenceTracingRiskLevelResultEntity ORDER BY calculatedAtMillis DESC LIMIT 1) UNION ALL SELECT * FROM (SELECT * FROM PresenceTracingRiskLevelResultEntity where riskState is not 0 ORDER BY calculatedAtMillis DESC LIMIT 1)")
    fun latestAndLastSuccessful(): Flow<List<PresenceTracingRiskLevelResultEntity>>

    @Query("SELECT * FROM PresenceTracingRiskLevelResultEntity ORDER BY calculatedAtMillis DESC LIMIT :limit")
    fun latestEntries(limit: Int): Flow<List<PresenceTracingRiskLevelResultEntity>>

    @Insert(onConflict = REPLACE)
    fun insert(entity: PresenceTracingRiskLevelResultEntity)

    @Query("DELETE FROM PresenceTracingRiskLevelResultEntity WHERE calculatedAtMillis < :calculatedAtMillis")
    suspend fun deleteOlderThan(calculatedAtMillis: Long)
}

@Entity
data class PresenceTracingRiskLevelResultEntity(
    @PrimaryKey @ColumnInfo(name = "calculatedAtMillis") val calculatedAtMillis: Long,
    val riskState: RiskState
)

private fun PresenceTracingRiskLevelResultEntity.toModel() = PtRiskLevelResult(
    calculatedAt = Instant.ofEpochMilli((calculatedAtMillis)),
    riskState = riskState
)

private fun PtRiskLevelResult.toEntity() = PresenceTracingRiskLevelResultEntity(
    calculatedAtMillis = calculatedAt.millis,
    riskState = riskState
)
