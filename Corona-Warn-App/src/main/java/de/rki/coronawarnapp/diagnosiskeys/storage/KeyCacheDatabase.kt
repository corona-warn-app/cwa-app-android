package de.rki.coronawarnapp.diagnosiskeys.storage

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import de.rki.coronawarnapp.util.database.CommonConverters
import javax.inject.Inject

@Database(
    entities = [CachedKeyFile::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class, CachedKeyFile.Type.Converter::class)
abstract class KeyCacheDatabase : RoomDatabase() {

    abstract fun cachedKeyFiles(): CachedKeyFileDao

    @Dao
    interface CachedKeyFileDao {
        @Query("SELECT * FROM keyfiles")
        suspend fun getAllEntries(): List<CachedKeyFile>

        @Query("SELECT * FROM keyfiles WHERE type = :type")
        suspend fun getEntriesForType(type: String): List<CachedKeyFile>

        @Query("DELETE FROM keyfiles")
        suspend fun clear()

        @Insert(onConflict = OnConflictStrategy.ABORT)
        suspend fun insertEntry(cachedKeyFile: CachedKeyFile)

        @Delete
        suspend fun deleteEntry(cachedKeyFile: CachedKeyFile)

        @Update(entity = CachedKeyFile::class)
        suspend fun updateDownloadState(update: CachedKeyFile.DownloadUpdate)
    }

    class Factory @Inject constructor(private val context: Context) {
        /**
         * The fallback behavior is to reset the app as we only store exposure summaries
         * and cached references that are non-critical to app operation.
         */
        fun create(): KeyCacheDatabase = Room
            .databaseBuilder(context, KeyCacheDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigrationFrom()
            .build()

    }

    companion object {
        private const val DATABASE_NAME = "keycache.db"
    }

}
