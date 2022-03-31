package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [
        AnalyticsExposureWindowEntity::class,
        AnalyticsScanInstanceEntity::class,
        AnalyticsReportedExposureWindowEntity::class
    ],
    version = 1
)
abstract class AnalyticsExposureWindowDatabase : RoomDatabase() {
    abstract fun analyticsExposureWindowDao(): AnalyticsExposureWindowDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create(): AnalyticsExposureWindowDatabase = Room
            .databaseBuilder(
                context,
                AnalyticsExposureWindowDatabase::class.java,
                "AnalyticsExposureWindow-db"
            )
            .fallbackToDestructiveMigration()
            .build()
    }
}

@Dao
interface AnalyticsExposureWindowDao {
    @Transaction
    @Query("SELECT * FROM AnalyticsExposureWindowEntity")
    suspend fun getAllNew(): List<AnalyticsExposureWindowEntityWrapper>

    @Query("SELECT * FROM AnalyticsExposureWindowEntity WHERE sha256Hash LIKE :sha256Hash")
    suspend fun getNew(sha256Hash: String): AnalyticsExposureWindowEntity?

    @Query("SELECT * FROM AnalyticsReportedExposureWindowEntity WHERE sha256Hash LIKE :sha256Hash")
    suspend fun getReported(sha256Hash: String): AnalyticsReportedExposureWindowEntity?

    @Query("SELECT * FROM AnalyticsReportedExposureWindowEntity")
    suspend fun getAllReported(): List<AnalyticsReportedExposureWindowEntity>

    @Delete
    suspend fun deleteExposureWindows(entities: List<AnalyticsExposureWindowEntity>)

    @Delete
    suspend fun deleteScanInstances(entities: List<AnalyticsScanInstanceEntity>)

    @Delete
    suspend fun deleteReported(entities: List<AnalyticsReportedExposureWindowEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExposureWindows(entities: List<AnalyticsExposureWindowEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScanInstances(entity: List<AnalyticsScanInstanceEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReported(entities: List<AnalyticsReportedExposureWindowEntity>): List<Long>

    @Transaction
    suspend fun insert(wrappers: List<AnalyticsExposureWindowEntityWrapper>) {
        insertExposureWindows(wrappers.map { it.exposureWindowEntity })
        insertScanInstances(wrappers.flatMap { it.scanInstanceEntities })
    }

    @Transaction
    suspend fun moveToReported(
        entities: List<AnalyticsExposureWindowEntityWrapper>,
        timestamp: Long
    ): List<AnalyticsReportedExposureWindowEntity> {
        val reported = entities.map {
            AnalyticsReportedExposureWindowEntity(
                it.exposureWindowEntity.sha256Hash,
                timestamp
            )
        }
        deleteExposureWindows(entities.map { it.exposureWindowEntity })
        deleteScanInstances(entities.flatMap { it.scanInstanceEntities })
        insertReported(reported)
        return reported
    }

    @Transaction
    suspend fun rollback(
        wrappers: List<AnalyticsExposureWindowEntityWrapper>,
        reported: List<AnalyticsReportedExposureWindowEntity>
    ) {
        deleteReported(reported)
        insertExposureWindows(wrappers.map { it.exposureWindowEntity })
        insertScanInstances(wrappers.flatMap { it.scanInstanceEntities })
    }

    @Query("DELETE FROM AnalyticsReportedExposureWindowEntity WHERE timestamp < :timestamp")
    suspend fun deleteReportedOlderThan(timestamp: Long)
}

class AnalyticsExposureWindowEntityWrapper(
    @Embedded val exposureWindowEntity: AnalyticsExposureWindowEntity,
    @Relation(parentColumn = PARENT_COLUMN, entityColumn = CHILD_COLUMN)
    val scanInstanceEntities: List<AnalyticsScanInstanceEntity>
)

@Entity
data class AnalyticsExposureWindowEntity(
    @PrimaryKey(autoGenerate = false) val sha256Hash: String,
    val calibrationConfidence: Int,
    val dateMillis: Long,
    val infectiousness: Int,
    val reportType: Int,
    val normalizedTime: Double,
    val transmissionRiskLevel: Int
)

@Entity
data class AnalyticsScanInstanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val fkSha256Hash: String,
    val minAttenuation: Int,
    val typicalAttenuation: Int,
    val secondsSinceLastScan: Int
)

@Entity
data class AnalyticsReportedExposureWindowEntity(
    @PrimaryKey(autoGenerate = false) val sha256Hash: String,
    val timestamp: Long
)

private const val PARENT_COLUMN = "sha256Hash"
private const val CHILD_COLUMN = "fkSha256Hash"
