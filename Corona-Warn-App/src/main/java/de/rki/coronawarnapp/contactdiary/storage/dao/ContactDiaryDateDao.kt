package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryDateEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContactDiaryDateDao: BaseRoomDao<ContactDiaryDateEntity>() {

    @Query("SELECT * FROM ContactDiaryDateEntity")
    abstract override fun allEntries(): Flow<List<ContactDiaryDateEntity>>

    @Query("DELETE FROM ContactDiaryDateEntity")
    abstract override suspend fun deleteAll()
}
