package de.rki.coronawarnapp.eventregistration.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("UnnecessaryAbstractClass")
abstract class TraceLocationDao {

    @Query("SELECT * FROM traceLocations")
    abstract fun allEntries(): Flow<List<TraceLocationEntity>>

    @Query("SELECT * FROM traceLocations WHERE guid = :guid")
    abstract fun entryForId(guid: String): TraceLocationEntity?

    @Insert
    abstract suspend fun insert(traceLocation: TraceLocationEntity)

    @Delete
    abstract suspend fun delete(traceLocation: TraceLocationEntity)

    @Query("DELETE FROM traceLocations")
    abstract suspend fun deleteAll()
}
