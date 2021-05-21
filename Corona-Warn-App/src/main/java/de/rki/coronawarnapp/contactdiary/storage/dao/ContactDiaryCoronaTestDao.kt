package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryTestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDiaryCoronaTestDao {

    @Query("SELECT * FROM corona_tests")
    fun allTests(): Flow<List<ContactDiaryTestEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTest(contactDiaryTestEntity: ContactDiaryTestEntity)

    @Query("DELETE FROM corona_tests")
    suspend fun deleteAll()
}
