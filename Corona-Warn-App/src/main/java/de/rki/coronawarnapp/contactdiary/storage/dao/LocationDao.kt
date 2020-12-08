package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LocationDao : BaseRoomDao<LocationEntity>() {

    @Query("SELECT * FROM LocationEntity")
    abstract override fun allEntries(): Flow<LocationEntity>

    @Query("DELETE FROM LocationEntity")
    abstract override suspend fun deleteAll()
}
