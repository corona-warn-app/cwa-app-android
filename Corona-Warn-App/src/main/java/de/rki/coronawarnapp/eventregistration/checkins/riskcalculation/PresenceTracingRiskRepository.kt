package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import de.rki.coronawarnapp.eventregistration.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.Days
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceTracingRiskRepository @Inject constructor(
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator,
    private val databaseFactory: TraceLocationDatabase.Factory,
    private val timeStamper: TimeStamper
) {

    private val database by lazy {
        databaseFactory.create()
    }

    private val traceTimeIntervalMatchDao by lazy {
        database.traceTimeIntervalMatchDao()
    }

    private val normalizedTime = traceTimeIntervalMatchDao.allEntries().map {
        it.map {
            it.toModel()
        }
    }.map {
        presenceTracingRiskCalculator.calculateNormalizedTime(it)
    }

    val traceLocationCheckInRiskStates: Flow<List<TraceLocationCheckInRisk>> =
        normalizedTime.map {
            presenceTracingRiskCalculator.calculateRisk(it)
        }

    val presenceTracingDayRisk: Flow<List<PresenceTracingDayRisk>> =
        normalizedTime.map {
            presenceTracingRiskCalculator.calculateAggregatedRiskPerDay(it)
        }

    suspend fun replaceAllMatches(list: List<CheckInOverlap>) {
        traceTimeIntervalMatchDao.deleteAll()
        addAll(list)
    }

    private suspend fun addAll(list: List<CheckInOverlap>) {
        traceTimeIntervalMatchDao.insert(list.map { it.toEntity() })
    }

    suspend fun deleteStaleMatches() {
        val endTime = timeStamper.nowUTC.minus(Days.days(15).toStandardDuration())
        traceTimeIntervalMatchDao.deleteOlderThan(endTime.millis)
    }

    suspend fun deleteAllMatches() {
        traceTimeIntervalMatchDao.deleteAll()
    }
}

@Dao
interface TraceTimeIntervalMatchDao {

    @Query("SELECT * FROM TraceTimeIntervalMatchEntity")
    fun allEntries(): Flow<List<TraceTimeIntervalMatchEntity>>

    @Query("DELETE FROM TraceTimeIntervalMatchEntity")
    suspend fun deleteAll()

    @Query("DELETE FROM TraceTimeIntervalMatchEntity WHERE endTimeMillis < :endTimeMillis")
    suspend fun deleteOlderThan(endTimeMillis: Long)

    @Insert
    suspend fun insert(entities: List<TraceTimeIntervalMatchEntity>)
}

@Entity
data class TraceTimeIntervalMatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ForeignKey(
        entity = TraceLocationCheckInEntity::class,
        parentColumns = ["id"],
        childColumns = ["checkInId"],
        onDelete = ForeignKey.CASCADE,
        deferred = true
    )
    val checkInId: Long,
    val traceWarningPackageId: Long,
    val transmissionRiskLevel: Int,
    val startTimeMillis: Long,
    val endTimeMillis: Long
)

private fun CheckInOverlap.toEntity() = TraceTimeIntervalMatchEntity(
    checkInId = checkInId,
    traceWarningPackageId = traceWarningPackageId,
    transmissionRiskLevel = transmissionRiskLevel,
    startTimeMillis = startTime.millis,
    endTimeMillis = endTime.millis
)

private fun TraceTimeIntervalMatchEntity.toModel() = CheckInOverlap(
    checkInId = checkInId,
    traceWarningPackageId = traceWarningPackageId,
    transmissionRiskLevel = transmissionRiskLevel,
    startTime = Instant.ofEpochMilli(startTimeMillis),
    endTime = Instant.ofEpochMilli(endTimeMillis)
)
