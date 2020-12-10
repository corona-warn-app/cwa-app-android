package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitEntity
import kotlinx.coroutines.flow.Flow

abstract class ContactDiaryLocationVisitDao : BaseRoomDao<ContactDiaryLocationVisitEntity>() {

    @Query("SELECT * FROM ContactDiaryLocationVisitEntity")
    abstract override fun allEntries(): Flow<List<ContactDiaryLocationVisitEntity>>

    @Query("SELECT * FROM ContactDiaryLocationVisitEntity WHERE id = :id")
    abstract override suspend fun entityForId(id: Long): ContactDiaryLocationVisitEntity

    @Query("DELETE FROM ContactDiaryLocationVisitEntity")
    abstract override suspend fun deleteAll()
}
