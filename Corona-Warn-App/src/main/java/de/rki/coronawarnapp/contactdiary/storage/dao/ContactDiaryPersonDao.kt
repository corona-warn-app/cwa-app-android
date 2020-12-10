package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContactDiaryPersonDao : BaseRoomDao<ContactDiaryPersonEntity>() {

    @Query("SELECT * FROM ContactDiaryPersonEntity")
    abstract override fun allEntries(): Flow<ContactDiaryPersonEntity>

    @Query("DELETE FROM ContactDiaryPersonEntity")
    abstract override suspend fun deleteAll()

    @Delete
    abstract suspend fun delete(entities: List<ContactDiaryPersonEntity>)
}
