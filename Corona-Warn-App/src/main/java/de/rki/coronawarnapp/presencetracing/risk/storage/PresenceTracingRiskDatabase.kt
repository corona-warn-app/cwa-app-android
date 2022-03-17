package de.rki.coronawarnapp.presencetracing.risk.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import javax.inject.Inject

@Database(
    entities = [
        TraceTimeIntervalMatchEntity::class,
        PresenceTracingRiskLevelResultEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(RiskStateConverter::class)
abstract class PresenceTracingRiskDatabase : RoomDatabase() {

    abstract fun presenceTracingRiskLevelResultDao(): PresenceTracingRiskLevelResultDao
    abstract fun traceTimeIntervalMatchDao(): TraceTimeIntervalMatchDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create() = Room
            .databaseBuilder(context, PresenceTracingRiskDatabase::class.java, DATABASE_NAME)
            .addMigrations(PresenceTracingRiskDatabaseMigration1To2)
            .build()
    }

    companion object {
        private const val DATABASE_NAME = "PresenceTracingRisk_db"
    }
}

object PresenceTracingRiskDatabaseMigration1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            performMigration(database)
        } catch (e: Exception) {
            Timber.e(e, "Migration 1->2 failed")
            e.report(ExceptionCategory.INTERNAL, "PresenceTracingRiskDatabase migration failed.")
            throw e
        }
    }

    private fun performMigration(database: SupportSQLiteDatabase) = with(database) {
        execSQL(
            "ALTER TABLE `PresenceTracingRiskLevelResultEntity` " +
                "ADD COLUMN `calculatedFromMillis` INTEGER NOT NULL DEFAULT 0"
        )
    }
}
