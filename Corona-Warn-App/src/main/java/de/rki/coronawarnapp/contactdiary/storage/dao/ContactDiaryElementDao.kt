package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryDate
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementLocationXRef
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementPersonXRef
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContactDiaryElementDao : BaseRoomDao<ContactDiaryElementEntity>() {

    @Query("SELECT * FROM ContactDiaryDate")
    abstract override fun allEntries(): Flow<ContactDiaryElementEntity>

    @Query("DELETE FROM ContactDiaryDate")
    abstract override suspend fun deleteAll()

    @Transaction
    override suspend fun insert(entity: ContactDiaryElementEntity) {
        insert(entity.contactDiaryDate)

        entity
            .locations
            .map { ContactDiaryElementLocationXRef(entity.date, it.locationId) }
            .forEach { insert(it) }

        entity
            .people
            .map { ContactDiaryElementPersonXRef(entity.date, it.personId) }
            .forEach { insert(it) }
    }

    @Transaction
    override suspend fun insertAll(entities: List<ContactDiaryElementEntity>) = entities.forEach { insert(it) }

    @Insert
    abstract suspend fun insert(contactDiaryElementLocationXRef: ContactDiaryElementLocationXRef)

    @Insert
    abstract suspend fun insert(contactDiaryElementPersonXRef: ContactDiaryElementPersonXRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(contactDiaryDate: ContactDiaryDate)
}
