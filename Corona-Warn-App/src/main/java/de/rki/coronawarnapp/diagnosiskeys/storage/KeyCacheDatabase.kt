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
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [CachedKeyInfo::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class, CachedKeyInfo.Type.Converter::class)
abstract class KeyCacheDatabase : RoomDatabase() {

    abstract fun cachedKeyFiles(): CachedKeyFileDao

    @Dao
    interface CachedKeyFileDao {
        @Query("SELECT * FROM keyfiles")
        suspend fun getAllEntries(): List<CachedKeyInfo>

        @Query("SELECT * FROM keyfiles WHERE type = :type")
        suspend fun getEntriesForType(type: String): List<CachedKeyInfo>

        @Query("DELETE FROM keyfiles")
        suspend fun clear()

        @Insert(onConflict = OnConflictStrategy.ABORT)
        suspend fun insertEntry(cachedKeyInfo: CachedKeyInfo)

        @Delete
        suspend fun deleteEntry(cachedKeyInfo: CachedKeyInfo)

        @Update(entity = CachedKeyInfo::class)
        suspend fun updateDownloadState(update: CachedKeyInfo.DownloadUpdate)
    }

    class Factory @Inject constructor(@AppContext private val context: Context) {
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
