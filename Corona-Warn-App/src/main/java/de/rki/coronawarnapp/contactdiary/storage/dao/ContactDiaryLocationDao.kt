package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryContactDiaryLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContactDiaryLocationDao : BaseRoomDao<ContactDiaryContactDiaryLocationEntity>() {

    @Query("SELECT * FROM ContactDiaryContactDiaryLocationEntity")
    abstract override fun allEntries(): Flow<ContactDiaryContactDiaryLocationEntity>

    @Query("DELETE FROM ContactDiaryContactDiaryLocationEntity")
    abstract override suspend fun deleteAll()
}
