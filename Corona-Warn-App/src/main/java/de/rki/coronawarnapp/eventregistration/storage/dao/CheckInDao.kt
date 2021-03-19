package de.rki.coronawarnapp.eventregistration.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
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

    @Query("DELETE FROM checkin")
    suspend fun deleteAll()

    @Query("DELETE FROM checkin WHERE id in (:idList)")
    suspend fun deleteByIds(idList: List<Long>)
}
