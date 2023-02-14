package de.rki.coronawarnapp.presencetracing.warning.storage

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.util.database.CommonConverters
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@Dao
interface TraceWarningPackageDao {

    @Query("SELECT * FROM TraceWarningPackageMetadata")
    fun getAllMetaData(): Flow<List<TraceWarningPackageMetadata>>

    @Query("SELECT * FROM TraceWarningPackageMetadata WHERE location = :location")
    suspend fun getAllMetaDataForLocation(location: String): List<TraceWarningPackageMetadata>

    @Query("SELECT * FROM TraceWarningPackageMetadata WHERE id = :packageId")
    suspend fun get(packageId: WarningPackageId): TraceWarningPackageMetadata?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: TraceWarningPackageMetadata)

    @Update(entity = TraceWarningPackageMetadata::class)
    suspend fun updateMetaData(update: TraceWarningPackageMetadata.UpdateDownload)

    @Update(entity = TraceWarningPackageMetadata::class)
    suspend fun updateMetaData(update: TraceWarningPackageMetadata.UpdateProcessed)

    @Query("DELETE FROM TraceWarningPackageMetadata WHERE id in (:packageIds)")
    suspend fun deleteByIds(packageIds: List<WarningPackageId>)

    @Query("DELETE FROM TraceWarningPackageMetadata")
    suspend fun clear()
}

@Database(
    entities = [TraceWarningPackageMetadata::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class)
abstract class TraceWarningDatabase : RoomDatabase() {

    abstract fun traceWarningPackageDao(): TraceWarningPackageDao

    class Factory @Inject constructor(@ApplicationContext private val context: Context) {
        fun create() = Room
            .databaseBuilder(context, TraceWarningDatabase::class.java, "TraceWarning_db")
            .build()
    }
}
