package de.rki.coronawarnapp.eventregistration.checkins.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.rki.coronawarnapp.eventregistration.checkins.storage.entity.EventCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EventCheckInDao {

    @Query("SELECT * FROM checkin")
    abstract fun allEntries(): Flow<List<EventCheckInEntity>>

    @Insert
    abstract suspend fun insert(entity: EventCheckInEntity): Long

    @Update
    abstract suspend fun update(entity: EventCheckInEntity)

    @Query("DELETE FROM checkin")
    abstract suspend fun deleteAll()
}
