package de.rki.coronawarnapp.presencetracing.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import timber.log.Timber

/**
 * Migrates the presence tracing database from version 1 to 2.
 * The additional attribute:
 * PresenceTracingCheckInEntity.submissionConsent was added
 */
object PresenceTracingDatabaseMigration1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Timber.i("Attempting migration 1->2...")
            performMigration(database)
            Timber.i("Migration 1->2 successful.")
        } catch (e: Exception) {
            Timber.e(e, "Migration 1->2 failed")
            e.report(ExceptionCategory.INTERNAL, "PresenceTracing database migration failed.")
            throw e
        }
    }

    private fun performMigration(database: SupportSQLiteDatabase) = with(database) {
        Timber.d("Running MIGRATION_1_2")

        migrateTraceLocationCheckInEntity()
    }

    private val migrateTraceLocationCheckInEntity: SupportSQLiteDatabase.() -> Unit = {
        Timber.d("Table 'checkin': Add column 'submissionConsent'")
        execSQL("ALTER TABLE `checkin` ADD COLUMN `submissionConsent` INTEGER NOT NULL DEFAULT '0'")
    }
}
