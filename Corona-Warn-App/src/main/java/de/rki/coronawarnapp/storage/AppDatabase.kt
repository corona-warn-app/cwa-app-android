package de.rki.coronawarnapp.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.storage.keycache.KeyCacheDao
import de.rki.coronawarnapp.storage.keycache.KeyCacheEntity
import de.rki.coronawarnapp.storage.tracing.TracingIntervalDao
import de.rki.coronawarnapp.storage.tracing.TracingIntervalEntity
import de.rki.coronawarnapp.util.Converters
import de.rki.coronawarnapp.util.security.SecurityHelper
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [ExposureSummaryEntity::class, KeyCacheEntity::class, TracingIntervalEntity::class],
    version = 1,
    exportSchema = false
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

        fun resetInstance(context: Context) = run { instance = null }.also { getInstance(context) }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(SupportFactory(SecurityHelper.getDBPassword()))
                .build()
        }
    }
}
