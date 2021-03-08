package de.rki.coronawarnapp.eventregistration.checkins.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.rki.coronawarnapp.eventregistration.checkins.storage.entity.EventCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventCheckInDao {

    @Query("SELECT * FROM checkin")
    fun allEntries(): Flow<List<EventCheckInEntity>>

    @Insert
    suspend fun insert(entity: EventCheckInEntity): Long

    @Update
    suspend fun update(entity: EventCheckInEntity)

    @Query("DELETE FROM checkin")
    suspend fun deleteAll()
}
