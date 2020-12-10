package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryContactDiaryPersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContactDiaryPersonDao : BaseRoomDao<ContactDiaryContactDiaryPersonEntity>() {

    @Query("SELECT * FROM ContactDiaryContactDiaryPersonEntity")
    abstract override fun allEntries(): Flow<ContactDiaryContactDiaryPersonEntity>

    @Query("DELETE FROM ContactDiaryContactDiaryPersonEntity")
    abstract override suspend fun deleteAll()
}
