package de.rki.coronawarnapp.coronatestjournal.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TestJournalDao {

    @Query("SELECT * FROM testjournal")
    fun allTests(): Flow<List<TestJournalEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTest(testJournalEntity: TestJournalEntity)

    @Query("DELETE FROM testjournal")
    suspend fun deleteAll()
}
