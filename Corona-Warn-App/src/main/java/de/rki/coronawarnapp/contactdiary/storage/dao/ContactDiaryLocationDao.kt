package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContactDiaryLocationDao : BaseRoomDao<ContactDiaryLocationEntity, ContactDiaryLocationEntity>() {

    @Query("SELECT * FROM locations")
    abstract override fun allEntries(): Flow<List<ContactDiaryLocationEntity>>

    @Query("SELECT * FROM locations WHERE locationId = :id")
    abstract override suspend fun entityForId(id: Long): ContactDiaryLocationEntity

    @Query("DELETE FROM locations")
    abstract override suspend fun deleteAll()
}
