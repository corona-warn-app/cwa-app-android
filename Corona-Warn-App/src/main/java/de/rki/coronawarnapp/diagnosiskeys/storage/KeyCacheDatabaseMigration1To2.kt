package de.rki.coronawarnapp.diagnosiskeys.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import timber.log.Timber

object KeyCacheDatabaseMigration1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            performMigration(database)
        } catch (e: Exception) {
            Timber.e(e, "Migration 1->2 failed")
            e.report(ExceptionCategory.INTERNAL, "KeyCacheDatabase migration 1 to 2 failed.")
            throw e
        }
    }

    private fun performMigration(database: SupportSQLiteDatabase) = with(database) {
        execSQL(
            "ALTER TABLE `keyfiles` " +
                "ADD COLUMN `checkedForExposures` INTEGER NOT NULL DEFAULT 0" // false
        )
    }
}
