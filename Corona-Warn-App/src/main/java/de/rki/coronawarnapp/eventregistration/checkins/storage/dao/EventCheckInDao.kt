package de.rki.coronawarnapp.eventregistration.checkins.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.rki.coronawarnapp.contactdiary.storage.dao.BaseRoomDao
import de.rki.coronawarnapp.eventregistration.checkins.storage.entity.EventCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EventCheckInDao : BaseRoomDao<EventCheckInEntity, EventCheckInEntity>() {

    @Query("SELECT * FROM checkin")
    abstract override fun allEntries(): Flow<List<EventCheckInEntity>>

    @Insert
    abstract override suspend fun insert(entity: EventCheckInEntity): Long

    @Update
    abstract override suspend fun update(entity: EventCheckInEntity)

    @Query("DELETE FROM checkin")
    abstract override suspend fun deleteAll()
}
