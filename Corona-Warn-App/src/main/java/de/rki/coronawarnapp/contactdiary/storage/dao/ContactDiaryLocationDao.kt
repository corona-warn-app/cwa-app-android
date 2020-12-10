package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContactDiaryLocationDao : BaseRoomDao<ContactDiaryLocationEntity>() {

    @Query("SELECT * FROM ContactDiaryLocationEntity")
    abstract override fun allEntries(): Flow<List<ContactDiaryLocationEntity>>

    @Query("SELECT * FROM ContactDiaryLocationEntity WHERE locationId = :id")
    abstract override suspend fun entityForId(id: Long): ContactDiaryLocationEntity

    @Query("DELETE FROM ContactDiaryLocationEntity")
    abstract override suspend fun deleteAll()
}
