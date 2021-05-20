package de.rki.coronawarnapp.coronatestjournal.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {

    @Query("SELECT * FROM testresults")
    fun allEntries(): Flow<List<TestResultEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEntry(testResultEntity: TestResultEntity)

}
