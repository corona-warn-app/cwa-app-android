package de.rki.coronawarnapp.submission.data.tekhistory.internal

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

@Suppress("MaxLineLength")
@Database(
    entities = [
        TEKEntryDao::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    CommonConverters::class
)
abstract class TEKHistoryDatabase : RoomDatabase() {

    abstract fun tekHistory(): TEKHistoryDao

    @Dao
    interface TEKHistoryDao {
        @Transaction
        @Query("SELECT * FROM tek_history")
        fun allEntries(): Flow<List<TEKEntryDao>>

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        suspend fun insertEntry(riskResultDao: TEKEntryDao)
    }

    class Factory @Inject constructor(@AppContext private val context: Context) {

        fun create(): TEKHistoryDatabase {
            Timber.d("Instantiating temporary exposure key history database.")
            return Room
                .databaseBuilder(context, TEKHistoryDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigrationFrom()
                .build()
        }
    }

    companion object {
        private const val DATABASE_NAME = "temporary_exposure_keys.db"
    }
}
