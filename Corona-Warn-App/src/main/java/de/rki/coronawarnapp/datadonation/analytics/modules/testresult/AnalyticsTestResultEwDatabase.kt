package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
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
        AnalyticsTestResultEwEntity::class,
        AnalyticsTestResultScanInstanceEntity::class
    ],
    version = 1
)
abstract class AnalyticsTestResultEwDatabase : RoomDatabase() {
    abstract fun analyticsExposureWindowDao(): AnalyticsTestResultEWDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create(): AnalyticsTestResultEwDatabase = Room
            .databaseBuilder(
                context,
                AnalyticsTestResultEwDatabase::class.java,
                "AnalyticsTestResultEw-db"
            )
            .fallbackToDestructiveMigration()
            .build()
    }
}

@Dao
interface AnalyticsTestResultEWDao {
    @Transaction
    @Query("SELECT * FROM AnalyticsTestResultEwEntity")
    suspend fun getAll(): List<AnalyticsTestResultEwEntityWrapper>

    @Delete
    suspend fun deleteScanInstances(entities: List<AnalyticsTestResultScanInstanceEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExposureWindows(entities: List<AnalyticsTestResultEwEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScanInstances(entity: List<AnalyticsTestResultScanInstanceEntity>): List<Long>

    @Transaction
    suspend fun insert(wrappers: List<AnalyticsTestResultEwEntityWrapper>) {
        insertExposureWindows(wrappers.map { it.exposureWindowEntity })
        insertScanInstances(wrappers.flatMap { it.scanInstanceEntities })
    }

    @Query("DELETE FROM AnalyticsTestResultEwEntity")
    suspend fun deleteAll()
}

class AnalyticsTestResultEwEntityWrapper(
    @Embedded val exposureWindowEntity: AnalyticsTestResultEwEntity,
    @Relation(parentColumn = PARENT_COLUMN, entityColumn = CHILD_COLUMN)
    val scanInstanceEntities: List<AnalyticsTestResultScanInstanceEntity>
)

@Entity
data class AnalyticsTestResultEwEntity(
    @PrimaryKey(autoGenerate = false) val sha256Hash: String,
    val calibrationConfidence: Int,
    val dateMillis: Long,
    val infectiousness: Int,
    val reportType: Int,
    val normalizedTime: Double,
    val transmissionRiskLevel: Int
)

@Entity
data class AnalyticsTestResultScanInstanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ForeignKey(
        entity = AnalyticsTestResultEwEntity::class,
        parentColumns = [PARENT_COLUMN],
        childColumns = [CHILD_COLUMN],
        onDelete = ForeignKey.CASCADE,
        deferred = true
    )
    val fkSha256Hash: String,
    val minAttenuation: Int,
    val typicalAttenuation: Int,
    val secondsSinceLastScan: Int
)

private const val PARENT_COLUMN = "sha256Hash"
private const val CHILD_COLUMN = "fkSha256Hash"
