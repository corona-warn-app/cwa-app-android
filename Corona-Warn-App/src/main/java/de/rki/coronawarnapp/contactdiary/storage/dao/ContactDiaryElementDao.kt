package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementLocationXRef
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryElementPersonXRef
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDiaryElementDao {

    @Transaction
    @Query("SELECT * FROM ContactDiaryDateEntity")
    fun allEntries(): Flow<ContactDiaryElementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContactDiaryElementPersonXRef(contactDiaryElementPersonXRef: ContactDiaryElementPersonXRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContactDiaryElementLocationXRef(contactDiaryElementLocationXRef: ContactDiaryElementLocationXRef)
}
