package de.rki.coronawarnapp.contactdiary.storage.internal.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import timber.log.Timber

/**
 * Migrates the contact diary database from schema version 2 to schema version 3.
 * We are adding additional columns for new optional attributes
 * Location: TraceLocationGUID
 * LocationVisit: CheckInID
 */
object ContactDiaryDatabaseMigration2To3 : Migration(2, 3) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Timber.i("Attempting migration 2->3...")
            performMigration(database)
            Timber.i("Migration 2->3 successful.")
        } catch (e: Exception) {
            Timber.e(e, "Migration 2->3 failed")
            e.report(ExceptionCategory.INTERNAL, "ContactDiary database migration failed.")
            throw e
        }
    }

    private fun performMigration(database: SupportSQLiteDatabase) = with(database) {
        Timber.d("Running MIGRATION_2_3")

        migrateLocationsTable()
        migrateLocationVisitTable()
    }

    private val migrateLocationsTable: SupportSQLiteDatabase.() -> Unit = {
        Timber.d("Table 'locations': Add column 'traceLocationID'")
        execSQL("ALTER TABLE `locations` ADD COLUMN `traceLocationID` TEXT")
    }

    private val migrateLocationVisitTable: SupportSQLiteDatabase.() -> Unit = {
        Timber.d("Table 'locationvisits': Add column 'checkInID'")
        execSQL("ALTER TABLE `locationvisits` ADD COLUMN `checkInID` INTEGER")
    }
}
