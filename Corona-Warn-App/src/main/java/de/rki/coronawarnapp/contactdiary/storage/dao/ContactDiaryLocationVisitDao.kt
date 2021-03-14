package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitWrapper
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

@Dao
abstract class ContactDiaryLocationVisitDao :
    BaseRoomDao<ContactDiaryLocationVisitEntity, ContactDiaryLocationVisitWrapper>() {

    @Transaction
    @Query("SELECT * FROM locationvisits")
    abstract override fun allEntries(): Flow<List<ContactDiaryLocationVisitWrapper>>

    @Transaction
    @Query("SELECT * FROM locationvisits WHERE id = :id")
    abstract override suspend fun entityForId(id: Long): ContactDiaryLocationVisitWrapper

    @Transaction
    @Query("SELECT * FROM locationvisits WHERE date = :date")
    abstract fun entitiesForDate(date: LocalDate): Flow<List<ContactDiaryLocationVisitWrapper>>

    @Query("DELETE FROM locationvisits")
    abstract override suspend fun deleteAll()
}
