package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitEntity
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

@Dao
abstract class ContactDiaryLocationVisitDao : BaseRoomDao<ContactDiaryLocationVisitEntity>() {

    @Query("SELECT * FROM ContactDiaryLocationVisitEntity")
    abstract override fun allEntries(): Flow<List<ContactDiaryLocationVisitEntity>>

    @Query("SELECT * FROM ContactDiaryLocationVisitEntity WHERE id = :id")
    abstract override suspend fun entityForId(id: Long): ContactDiaryLocationVisitEntity

    @Query("SELECT * FROM ContactDiaryLocationVisitEntity WHERE date = :date")
    abstract fun entitiesForDate(date: LocalDate): Flow<List<ContactDiaryLocationVisitEntity>>

    @Query("DELETE FROM ContactDiaryLocationVisitEntity")
    abstract override suspend fun deleteAll()
}
