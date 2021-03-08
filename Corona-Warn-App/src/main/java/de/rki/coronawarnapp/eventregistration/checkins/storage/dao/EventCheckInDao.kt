package de.rki.coronawarnapp.eventregistration.checkins.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.dao.BaseRoomDao
import de.rki.coronawarnapp.eventregistration.checkins.storage.entity.EventCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EventCheckInDao : BaseRoomDao<EventCheckInEntity, EventCheckInEntity>() {

    @Query("SELECT * FROM checkin")
    abstract override fun allEntries(): Flow<List<EventCheckInEntity>>

    @Query("DELETE FROM checkin")
    abstract override suspend fun deleteAll()
}
