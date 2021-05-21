package de.rki.coronawarnapp.contactdiary.storage.internal.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import timber.log.Timber

/**
 * Migrates the contact diary database from schema version 3 to schema version 4.
 * We are adding additional table for storing test results
 */
@Suppress("MaxLineLength")
object ContactDiaryDatabaseMigration3To4 : Migration(3, 4) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Timber.i("Attempting migration 3->4...")
            performMigration(database)
            Timber.i("Migration 3->4 successful.")
        } catch (e: Exception) {
            Timber.e(e, "Migration 3->4 failed")
            e.report(ExceptionCategory.INTERNAL, "ContactDiary database migration failed.")
            throw e
        }
    }

    private fun performMigration(database: SupportSQLiteDatabase) = with(database) {
        Timber.d("Running MIGRATION_3_4")

        migrateTestTable()
    }

    private val migrateTestTable: SupportSQLiteDatabase.() -> Unit = {
        Timber.d("Create 'corona_tests' table")
        execSQL("CREATE TABLE IF NOT EXISTS corona_tests (`id` TEXT NOT NULL, `testType` TEXT NOT NULL, `result` TEXT NOT NULL, `time` TEXT NOT NULL, PRIMARY KEY(`id`))")
    }
}
