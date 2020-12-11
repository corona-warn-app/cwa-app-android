package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterWrapper
import kotlinx.coroutines.flow.Flow
import org.joda.time.LocalDate

@Dao
abstract class ContactDiaryPersonEncounterDao :
    BaseRoomDao<ContactDiaryPersonEncounterEntity, ContactDiaryPersonEncounterWrapper>() {

    @Transaction
    @Query("SELECT * FROM ContactDiaryPersonEncounterEntity")
    abstract override fun allEntries(): Flow<List<ContactDiaryPersonEncounterWrapper>>

    @Transaction
    @Query("SELECT * FROM ContactDiaryPersonEncounterEntity WHERE id = :id")
    abstract override suspend fun entityForId(id: Long): ContactDiaryPersonEncounterWrapper

    @Transaction
    @Query("SELECT * FROM ContactDiaryPersonEncounterEntity WHERE date = :date")
    abstract fun entitiesForDate(date: LocalDate): Flow<List<ContactDiaryPersonEncounterWrapper>>

    @Query("DELETE FROM ContactDiaryPersonEncounterEntity")
    abstract override suspend fun deleteAll()
}
