package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDiaryCoronaTestDao {

    @Query("SELECT * FROM corona_tests")
    fun allTests(): Flow<List<ContactDiaryCoronaTestEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTest(contactDiaryCoronaTestEntity: ContactDiaryCoronaTestEntity)

    @Query("DELETE FROM corona_tests")
    suspend fun deleteAll()
}
