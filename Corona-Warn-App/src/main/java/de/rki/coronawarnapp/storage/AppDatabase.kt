package de.rki.coronawarnapp.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.storage.keycache.KeyCacheDao
import de.rki.coronawarnapp.storage.keycache.KeyCacheEntity
import de.rki.coronawarnapp.storage.keycache.KeyCacheRepository
import de.rki.coronawarnapp.storage.tracing.TracingIntervalDao
import de.rki.coronawarnapp.storage.tracing.TracingIntervalEntity
import de.rki.coronawarnapp.storage.tracing.TracingIntervalRepository
import de.rki.coronawarnapp.util.Converters
import de.rki.coronawarnapp.util.security.SecurityHelper
import net.sqlcipher.database.SupportFactory
import java.io.File

@Database(
    entities = [ExposureSummaryEntity::class, KeyCacheEntity::class, TracingIntervalEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exposureSummaryDao(): ExposureSummaryDao
    abstract fun dateDao(): KeyCacheDao
    abstract fun tracingIntervalDao(): TracingIntervalDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        fun resetInstance() = synchronized(this) {
            instance = null
        }

        fun reset(context: Context) {
            val path: String = context.getDatabasePath(DATABASE_NAME).path
            val dbFile = File(path)
            if (dbFile.exists()) {
                SQLiteDatabase.deleteDatabase(dbFile)
            }
            resetInstance()

            // reset also the repo instances
            KeyCacheRepository.resetInstance()
            TracingIntervalRepository.resetInstance()
            ExposureSummaryRepository.resetInstance()
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                /**
                 * The fallback behavior is to reset the app as we only store exposure summaries
                 * and cached references that are non-critical to app operation.
                 */
                .fallbackToDestructiveMigrationFrom()
                .openHelperFactory(SupportFactory(SecurityHelper.getDBPassword()))
                .build()
        }
    }
}
