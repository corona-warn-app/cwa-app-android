package de.rki.coronawarnapp.presencetracing.risk.storage

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.presencetracing.risk.RelevantCheckInsFilter
import de.rki.coronawarnapp.presencetracing.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInNormalizedTime
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningOverlap
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingDayRisk
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingRiskCalculator
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.plus
import org.joda.time.Days
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceTracingRiskRepository @Inject constructor(
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator,
    private val databaseFactory: PresenceTracingRiskDatabase.Factory,
    private val timeStamper: TimeStamper,
    private val relevantCheckInsFilter: RelevantCheckInsFilter,
    @AppScope private val appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
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

    val allOverlaps = traceTimeIntervalMatchDao.allMatches().map { entities ->
        entities.map { it.toCheckInWarningOverlap() }
    }

    private val normalizedTime: HotDataFlow<List<CheckInNormalizedTime>> = HotDataFlow(
        loggingTag = "PresenceTracingRiskRepository",
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.Lazily,
    ) {
        calculateNormalizedTime(allOverlaps.first())
    }

    private suspend fun calculateNormalizedTime(list: List<CheckInWarningOverlap>): List<CheckInNormalizedTime> {
        val relevantWarnings = relevantCheckInsFilter.filterCheckInWarnings(list)
        return presenceTracingRiskCalculator.calculateNormalizedTime(relevantWarnings)
    }

    val traceLocationCheckInRiskStates: Flow<List<TraceLocationCheckInRisk>> =
        normalizedTime.data.map {
            presenceTracingRiskCalculator.calculateCheckInRiskPerDay(it)
        }

    val presenceTracingDayRisk: Flow<List<PresenceTracingDayRisk>> =
        normalizedTime.data.map {
            presenceTracingRiskCalculator.calculateDayRisk(it)
        }

    /**
     * We delete warning packages after processing, we need to store the latest matches independent of success state
     */
    internal suspend fun reportCalculation(
        successful: Boolean,
        overlaps: List<CheckInWarningOverlap> = emptyList()
    ) {
        Timber.v("reportCalculation(successful=%b, overlaps=%s)", successful, overlaps)

        val nowUTC = timeStamper.nowUTC

        // delete stale matches from new packages, old matches are superseeded
        overlaps.map { it.traceWarningPackageId }.forEach {
            traceTimeIntervalMatchDao.deleteMatchesForPackage(it)
        }

        if (overlaps.isNotEmpty()) {
            traceTimeIntervalMatchDao.insert(overlaps.map { it.toTraceTimeIntervalMatchEntity() })
        }

        val result = if (successful) {
            val newNormalizedTime = calculateNormalizedTime(allOverlaps.first())
            val risk = presenceTracingRiskCalculator.calculateTotalRisk(newNormalizedTime)
            normalizedTime.updateBlocking {
                newNormalizedTime
            }
            PtRiskLevelResult(nowUTC, risk)
        } else {
            PtRiskLevelResult(nowUTC, RiskState.CALCULATION_FAILED)
        }
        addResult(result)
    }

    internal suspend fun deleteStaleData() {
        Timber.d("deleteStaleData()")
        traceTimeIntervalMatchDao.deleteOlderThan(fifteenDaysAgo.millis)
        riskLevelResultDao.deleteOlderThan(fifteenDaysAgo.millis)
    }

    suspend fun deleteAllMatches() {
        Timber.d("deleteAllMatches()")
        traceTimeIntervalMatchDao.deleteAll()
    }

    fun latestEntries(limit: Int) = riskLevelResultDao.latestEntries(limit).map { list ->
        list.sortAndComplementLatestResult()
    }

    fun allEntries() = riskLevelResultDao.allEntries().map { list ->
        list.sortAndComplementLatestResult()
    }

    private suspend fun List<PresenceTracingRiskLevelResultEntity>.sortAndComplementLatestResult() =
        sortedByDescending {
            it.calculatedAtMillis
        }
            .mapIndexed { index, entity ->
                if (index == 0) {
                    // add risk per day to the latest result
                    entity.toRiskLevelResult(
                        presenceTracingDayRisks = presenceTracingDayRisk.first(),
                        checkInWarningOverlaps = allOverlaps.first(),
                    )
                } else {
                    entity.toRiskLevelResult(
                        presenceTracingDayRisks = null,
                        checkInWarningOverlaps = null,
                    )
                }
            }

    private fun addResult(result: PtRiskLevelResult) {
        Timber.i("Saving risk calculation from ${result.calculatedAt} with result ${result.riskState}.")
        riskLevelResultDao.insert(result.toRiskLevelEntity())
    }

    private val fifteenDaysAgo: Instant
        get() = timeStamper.nowUTC.minus(Days.days(15).toStandardDuration())

    suspend fun clearAllTables() {
        Timber.i("Deleting all matches and results.")
        traceTimeIntervalMatchDao.deleteAll()
        riskLevelResultDao.deleteAll()
    }

    suspend fun clearResults() {
        Timber.i("Deleting all results.")
        riskLevelResultDao.deleteAll()
    }
}

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

    @Insert(onConflict = REPLACE)
    fun insert(entity: PresenceTracingRiskLevelResultEntity)

    @Query("DELETE FROM PresenceTracingRiskLevelResultEntity WHERE calculatedAtMillis < :calculatedAtMillis")
    suspend fun deleteOlderThan(calculatedAtMillis: Long)

    @Query("DELETE FROM PresenceTracingRiskLevelResultEntity")
    suspend fun deleteAll()
}

@Entity
data class PresenceTracingRiskLevelResultEntity(
    @PrimaryKey @ColumnInfo(name = "calculatedAtMillis") val calculatedAtMillis: Long,
    @ColumnInfo(name = "riskStateCode") val riskState: RiskState
)

private fun PresenceTracingRiskLevelResultEntity.toRiskLevelResult(
    presenceTracingDayRisks: List<PresenceTracingDayRisk>?,
    checkInWarningOverlaps: List<CheckInWarningOverlap>?
) = PtRiskLevelResult(
    calculatedAt = Instant.ofEpochMilli((calculatedAtMillis)),
    riskState = riskState,
    presenceTracingDayRisk = presenceTracingDayRisks,
    checkInWarningOverlaps = checkInWarningOverlaps,
)

private fun PtRiskLevelResult.toRiskLevelEntity() = PresenceTracingRiskLevelResultEntity(
    calculatedAtMillis = calculatedAt.millis,
    riskState = riskState
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
        private const val TAG = "PresenceTracingRiskRepository"
    }
}
