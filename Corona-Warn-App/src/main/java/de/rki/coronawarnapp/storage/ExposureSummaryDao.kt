package de.rki.coronawarnapp.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExposureSummaryDao {

    @Query("SELECT * FROM exposure_summary")
    suspend fun getExposureSummaryEntities(): List<ExposureSummaryEntity>

    @Query("SELECT * FROM exposure_summary ORDER BY id DESC LIMIT 1")
    suspend fun getLatestExposureSummary(): ExposureSummaryEntity?

    @Insert
    suspend fun insertExposureSummaryEntity(exposureSummaryEntity: ExposureSummaryEntity): Long
}
