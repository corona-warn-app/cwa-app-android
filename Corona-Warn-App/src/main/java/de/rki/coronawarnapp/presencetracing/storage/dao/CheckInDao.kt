package de.rki.coronawarnapp.presencetracing.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.toEntity
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.presencetracing.storage.entity.toCheckIn
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {

    @Query("SELECT * FROM checkin")
    fun allEntries(): Flow<List<TraceLocationCheckInEntity>>

    @Query("SELECT * FROM checkin WHERE id = :id")
    suspend fun entryForId(id: Long): TraceLocationCheckInEntity?

    @Insert
    suspend fun insert(entity: TraceLocationCheckInEntity): Long

    @Update
    suspend fun update(entity: TraceLocationCheckInEntity)

    @Transaction
    suspend fun updateEntityById(checkInId: Long, update: (CheckIn) -> CheckIn) {
        val current = entryForId(checkInId) ?: throw IllegalStateException("Entity $checkInId no longer exists.")

        val updated = update(current.toCheckIn()).also {
            if (it.id != checkInId) throw UnsupportedOperationException("Can't change entity id: $it")
        }.toEntity()

        update(updated)
    }

    @Update(entity = TraceLocationCheckInEntity::class)
    suspend fun updateEntity(update: TraceLocationCheckInEntity.SubmissionUpdate)

    @Update(entity = TraceLocationCheckInEntity::class)
    suspend fun updateSubmissionConsents(update: Collection<TraceLocationCheckInEntity.SubmissionConsentUpdate>)

    @Query("DELETE FROM checkin")
    suspend fun deleteAll()

    @Query("DELETE FROM checkin WHERE id in (:idList)")
    suspend fun deleteByIds(idList: List<Long>)
}
