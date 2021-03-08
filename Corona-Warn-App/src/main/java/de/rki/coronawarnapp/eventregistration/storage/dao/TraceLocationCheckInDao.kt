package de.rki.coronawarnapp.eventregistration.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TraceLocationCheckInDao {

    @Query("SELECT * FROM checkin")
    fun allEntries(): Flow<List<TraceLocationCheckInEntity>>

    @Insert
    suspend fun insert(entity: TraceLocationCheckInEntity): Long

    @Update
    suspend fun update(entity: TraceLocationCheckInEntity)

    @Query("DELETE FROM checkin")
    suspend fun deleteAll()
}
