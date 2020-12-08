package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PersonDao : BaseRoomDao<PersonEntity>() {

    @Query("SELECT * FROM PersonEntity")
    abstract override fun allEntries(): Flow<PersonEntity>

    @Query("DELETE FROM PersonEntity")
    abstract override suspend fun deleteAll()
}
