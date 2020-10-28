package de.rki.coronawarnapp.bugreporting.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.rki.coronawarnapp.bugreporting.event.BugEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DefaultBugDao : BugDao<BugEventEntity> {

    @Insert
    override suspend fun insertBugEvent(bugEvent: BugEventEntity)

    @Query("SELECT * FROM BugEventEntity WHERE id = :id")
    override fun getBugEvent(id: Long): Flow<BugEventEntity>

    @Query("SELECT * FROM BugEventEntity")
    override fun getAllBugEvents(): Flow<List<BugEventEntity>>

    @Query("DELETE FROM BugEventEntity")
    override suspend fun deleteAllBugEvents()

    @Query("DELETE FROM BugEventEntity WHERE id = :id")
    override suspend fun deleteBugEvent(id: Long)
}
