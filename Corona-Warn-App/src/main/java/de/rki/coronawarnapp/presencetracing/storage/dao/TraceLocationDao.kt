package de.rki.coronawarnapp.presencetracing.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("UnnecessaryAbstractClass")
abstract class TraceLocationDao {

    @Query("SELECT * FROM traceLocations")
    abstract fun allEntries(): Flow<List<TraceLocationEntity>>

    @Query("SELECT * FROM traceLocations WHERE id = :id")
    abstract fun entryForId(id: Long): TraceLocationEntity?

    @Insert
    abstract suspend fun insert(traceLocation: TraceLocationEntity): Long

    @Query("SELECT * FROM traceLocations WHERE id = :id")
    abstract suspend fun entityForId(id: Long): TraceLocationEntity

    @Delete
    abstract suspend fun delete(traceLocation: TraceLocationEntity)

    @Query("DELETE FROM traceLocations")
    abstract suspend fun deleteAll()
}
