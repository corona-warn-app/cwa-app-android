package de.rki.coronawarnapp.eventregistration.checkins.download

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class TraceTimeIntervalWarningRepository(
    @AppContext private val context: Context,
    private val factory : TraceWarningDatabase.Factory,
    private val timeStamper: TimeStamper
) {

    private val dao: TraceWarningPackageDao = factory.create().traceWarningPackageDao()

    val packageMetadataEntities: List<TraceWarningPackageMetadataEntity>
        get() = dao.getAll()

    fun markDownloadComplete(packageId: Long, eTag: String, file: File) {
        dao.update(packageId, eTag, file)
    }

    fun deleteFile(path: File) = path.delete()

    fun deleteStalePackage(packageIds: List<Long>) {
        packageIds.forEach {
            dao.deleteById(it)
        }
    }

    fun createMetadata(packageId: Long, location: LocationCode): TraceWarningPackageMetadataEntity {
        val metadata = TraceWarningPackageMetadataEntity(
            packageId = packageId,
            createdAt = timeStamper.nowUTC,
            absolutePath = getFile(packageId).absolutePath,
            location = location
        )
        dao.insert(metadata)
        return metadata
    }

    @Transaction
    fun deleteAllPackages() {
        dao.deleteAll()
    }

    private val storageDir by lazy {
        File(context.cacheDir, "trace_warning_packages").apply {
            if (!exists()) {
                if (mkdirs()) {
                    Timber.d("Trace warning package directory created: %s", this)
                } else {
                    throw IOException("Trace warning package directory creation failed: $this")
                }
            }
        }
    }

    private fun getFile(packageId: WarningPackageId): File = File(storageDir, "twp_$packageId.zip")

}

@Database(
    entities = [
        TraceWarningPackageMetadataEntity::class
    ],
    version = 1,
    exportSchema = true
)

abstract class TraceWarningDatabase : RoomDatabase() {

    abstract fun traceWarningPackageDao(): TraceWarningPackageDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create() = Room
            .databaseBuilder(context, TraceWarningDatabase::class.java, "TraceWarning_db")
            .build()
    }
}

@Dao
abstract class TraceWarningPackageDao {
    @Transaction
    fun deleteAll() {
        val all = getAll()
        all.forEach { metadata ->
            metadata.absolutePath.let {
                File(it).delete()
            }
        }
        delete(all)
    }

    @Transaction
    fun update(packageId: Long, eTag: String, file: File) {
        val metadata = get(packageId)
        metadata?.let {
            update(it.copy(eTag = eTag, absolutePath = file.absolutePath, isDownloadComplete = true))
        }
    }

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(entity: TraceWarningPackageMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(entity: TraceWarningPackageMetadataEntity)

    @Delete
    abstract fun delete(entities: List<TraceWarningPackageMetadataEntity>)

    @Query("DELETE FROM TraceWarningPackageMetadataEntity where packageId = :packageId")
    abstract fun deleteById(packageId: WarningPackageId)

    @Query("SELECT * FROM TraceWarningPackageMetadataEntity")
    abstract fun getAll(): List<TraceWarningPackageMetadataEntity>

    @Query("SELECT * FROM TraceWarningPackageMetadataEntity WHERE packageId = :packageId")
    abstract fun get(packageId: Long): TraceWarningPackageMetadataEntity?
}

@Entity
data class TraceWarningPackageMetadataEntity(
    @PrimaryKey val packageId: WarningPackageId,
    val createdAt: Instant,
    val location: LocationCode, // i.e. "DE"
    val eTag: String? = null,
    val absolutePath: String,
    val isDownloadComplete: Boolean = false
)
