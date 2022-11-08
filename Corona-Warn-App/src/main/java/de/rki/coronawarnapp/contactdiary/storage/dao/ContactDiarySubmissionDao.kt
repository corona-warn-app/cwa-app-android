package de.rki.coronawarnapp.contactdiary.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiarySubmissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDiarySubmissionDao {

    @Query("SELECT * FROM submissions")
    fun allSubmissions(): Flow<List<ContactDiarySubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubmission(submission: ContactDiarySubmissionEntity)

    @Delete
    suspend fun delete(submissions: List<ContactDiarySubmissionEntity>)
}
