package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.dao.BaseRoomDao
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import org.joda.time.Days
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceTimeIntervalMatchRepository @Inject constructor(
    private val databaseFactory: PresenceTracingDatabase.Factory,
    private val timeStamper: TimeStamper
) {

    private val database by lazy {
        databaseFactory.create()
    }

    private val traceTimeIntervalMatchDao by lazy {
        database.traceTimeIntervalMatchDao()
    }

    suspend fun addAll(list: List<CheckInOverlap>) {
        traceTimeIntervalMatchDao.insert(list.map { it.toEntity() })
    }

    suspend fun deleteStaleMatches() {
        val endTime = timeStamper.nowUTC.minus(Days.days(15).toStandardDuration())
        traceTimeIntervalMatchDao.deleteOlderThan(endTime.millis)
    }
}

@Dao
abstract class TraceTimeIntervalMatchDao : BaseRoomDao<TraceTimeIntervalMatchEntity, TraceTimeIntervalMatchEntity>() {

    @Query("SELECT * FROM TraceTimeIntervalMatchEntity")
    abstract override fun allEntries(): Flow<List<TraceTimeIntervalMatchEntity>>

    @Query("DELETE FROM TraceTimeIntervalMatchEntity")
    abstract override suspend fun deleteAll()

    @Query("DELETE FROM TraceTimeIntervalMatchEntity WHERE endTimeMillis < :endTimeMillis")
    abstract suspend fun deleteOlderThan(endTimeMillis: Long)
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

fun CheckInOverlap.toEntity() = TraceTimeIntervalMatchEntity(
    checkInId = checkInId,
    traceWarningPackageId = traceWarningPackageId,
    transmissionRiskLevel = transmissionRiskLevel,
    startTimeMillis = startTime.millis,
    endTimeMillis = endTime.millis
)
