package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContactDiaryElementDao : BaseRoomDao<ContactDiaryElementEntity>() {

    @Query("SELECT * FROM ContactDiaryElementEntity")
    abstract override fun allEntries(): Flow<ContactDiaryElementEntity>

    @Query("DELETE FROM ContactDiaryElementEntity")
    abstract override suspend fun deleteAll()
}
