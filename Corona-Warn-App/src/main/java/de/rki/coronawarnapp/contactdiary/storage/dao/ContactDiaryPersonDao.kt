package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContactDiaryPersonDao : BaseRoomDao<ContactDiaryPersonEntity, ContactDiaryPersonEntity>() {

    @Query("SELECT * FROM persons")
    abstract override fun allEntries(): Flow<List<ContactDiaryPersonEntity>>

    @Query("SELECT * FROM persons WHERE personId = :id")
    abstract override suspend fun entityForId(id: Long): ContactDiaryPersonEntity

    @Query("DELETE FROM persons")
    abstract override suspend fun deleteAll()
}
