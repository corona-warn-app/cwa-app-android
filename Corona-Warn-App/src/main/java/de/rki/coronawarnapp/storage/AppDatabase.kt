package de.rki.coronawarnapp.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.KeyCacheLegacyDao
import de.rki.coronawarnapp.diagnosiskeys.storage.legacy.KeyCacheLegacyEntity
import de.rki.coronawarnapp.storage.tracing.TracingIntervalDao
import de.rki.coronawarnapp.storage.tracing.TracingIntervalEntity
import de.rki.coronawarnapp.storage.tracing.TracingIntervalRepository
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.security.SecurityHelper
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SupportFactory
import java.io.File

@Database(
    entities = [
        ExposureSummaryEntity::class,
        KeyCacheLegacyEntity::class,
        TracingIntervalEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exposureSummaryDao(): ExposureSummaryDao
    abstract fun dateDao(): KeyCacheLegacyDao
    abstract fun tracingIntervalDao(): TracingIntervalDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
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
            val keyRepository = AppInjector.component.keyCacheRepository
            runBlocking { keyRepository.clear() } // TODO this is not nice
            TracingIntervalRepository.resetInstance()
            ExposureSummaryRepository.resetInstance()
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                /**
                 * The fallback behavior is to reset the app as we only store exposure summaries
                 * and cached references that are non-critical to app operation.
                 */
                .fallbackToDestructiveMigration()
                .openHelperFactory(SupportFactory(SecurityHelper.getDBPassword()))
                .build()
        }
    }
}
