package de.rki.coronawarnapp.eventregistration.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.rki.coronawarnapp.eventregistration.storage.entity.HostedEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("UnnecessaryAbstractClass")
abstract class HostedEventDao {

    @Query("SELECT * FROM hostedEvents")
    abstract fun allEntries(): Flow<List<HostedEventEntity>>

    @Insert
    abstract suspend fun insert(hostedEvent: HostedEventEntity)

    @Delete
    abstract suspend fun delete(hostedEvent: HostedEventEntity)

    @Query("DELETE FROM hostedEvents")
    abstract suspend fun deleteAll()
}
