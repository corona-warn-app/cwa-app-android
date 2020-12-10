package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterEntity
import kotlinx.coroutines.flow.Flow

abstract class ContactDiaryPersonEncounterDao : BaseRoomDao<ContactDiaryPersonEncounterEntity>() {

    @Query("SELECT * FROM ContactDiaryPersonEncounterEntity")
    abstract override fun allEntries(): Flow<List<ContactDiaryPersonEncounterEntity>>

    @Query("SELECT * FROM ContactDiaryPersonEncounterEntity WHERE id = :id")
    abstract override suspend fun entityForId(id: Long): ContactDiaryPersonEncounterEntity

    @Query("DELETE FROM ContactDiaryPersonEncounterEntity")
    abstract override suspend fun deleteAll()
}
