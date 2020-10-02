package de.rki.coronawarnapp.storage.tracing

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TracingIntervalDao {
    @Query("DELETE FROM tracing_interval WHERE `to` < :retentionTimestamp")
    suspend fun deleteOutdatedIntervals(retentionTimestamp: Long)

    @Query("SELECT * FROM tracing_interval")
    suspend fun getAllIntervals(): List<TracingIntervalEntity>

    @Insert
    suspend fun insertInterval(interval: TracingIntervalEntity)
}
