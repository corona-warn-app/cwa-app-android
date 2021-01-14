package de.rki.coronawarnapp.risk.storage.internal.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import timber.log.Timber

@Suppress("MaxLineLength")
object RiskResultDatabaseMigration2To3 : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Timber.i("Attempting migration 2->3...")
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `riskperdate` (`dateMillisSinceEpoch` INTEGER NOT NULL, `riskLevel` INTEGER NOT NULL, `minimumDistinctEncountersWithLowRisk` INTEGER NOT NULL, `minimumDistinctEncountersWithHighRisk` INTEGER NOT NULL, PRIMARY KEY(`dateMillisSinceEpoch`))"
            )
            Timber.i("Migration 2->3 successful.")
        } catch (e: Exception) {
            e.report(ExceptionCategory.INTERNAL, "Migration 2->3 failed. Could not create new table riskperdate")
            throw e
        }
    }
}
