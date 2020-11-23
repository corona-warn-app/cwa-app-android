package de.rki.coronawarnapp.risk.storage.internal

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

@Database(
    entities = [
        PersistedRiskResultDao::class,
        PersistedExposureWindowDao::class,
        PersistedExposureWindowDao.PersistedScanInstance::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    CommonConverters::class,
    PersistedRiskResultDao.Converter::class,
    PersistedRiskResultDao.PersistedAggregatedRiskResult.Converter::class
)
abstract class RiskResultDatabase : RoomDatabase() {

    abstract fun riskResults(): RiskResultsDao

    abstract fun exposureWindows(): ExposureWindowsDao

    @Dao
    interface RiskResultsDao {
        @Query("SELECT * FROM riskresults")
        fun allEntries(): Flow<List<PersistedRiskResultDao>>

        @Insert(onConflict = OnConflictStrategy.ABORT)
        suspend fun insertEntry(riskResultDao: PersistedRiskResultDao)

        @Query(
            "DELETE FROM riskresults where id NOT IN (SELECT id from riskresults ORDER BY calculatedAt DESC LIMIT :keep)"
        )
        suspend fun deleteOldest(keep: Int): Int
    }

    @Dao
    interface ExposureWindowsDao {
        @Query("SELECT * FROM exposurewindows")
        fun allEntries(): Flow<List<PersistedExposureWindowDao>>

        @Insert(onConflict = OnConflictStrategy.ABORT)
        suspend fun insertEntry(exposureWindowDao: PersistedExposureWindowDao)

        @Query(
            "DELETE FROM exposurewindows where id NOT IN (SELECT id from exposurewindows ORDER BY dateMillisSinceEpoch DESC LIMIT :keep)"
        )
        suspend fun deleteOldest(keep: Int): Int
    }

    class Factory @Inject constructor(@AppContext private val context: Context) {

        fun create(): RiskResultDatabase {
            Timber.d("Instantiating risk result database.")
            return Room
                .databaseBuilder(context, RiskResultDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigrationFrom()
                .build()
        }
    }

    companion object {
        private const val DATABASE_NAME = "riskresults.db"
    }
}
