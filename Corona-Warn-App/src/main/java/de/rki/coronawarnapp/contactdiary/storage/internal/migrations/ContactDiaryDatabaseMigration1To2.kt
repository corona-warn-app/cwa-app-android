package de.rki.coronawarnapp.contactdiary.storage.internal.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import timber.log.Timber

/**
 * Migrates the contact diary database from schema version 1 to schema version 2.
 * We are adding additional columns for new optional attributes
 * Person: PhoneNumber, Email
 * Location: PhoneNumber, Email
 * PersonEncounter: DurationType, Mask?, Outside?, Comment
 * LocationVisit: Duration, Comment
 */
object ContactDiaryDatabaseMigration1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Timber.i("Attempting migration 1->2...")
            performMigration(database)
            Timber.i("Migration 1->2 successful.")
        } catch (e: Exception) {
            Timber.e(e, "Migration 1->2 failed")
            e.report(ExceptionCategory.INTERNAL, "ContactDiary database migration failed.")
            throw e
        }
    }

    private fun performMigration(database: SupportSQLiteDatabase) = with(database) {
        Timber.d("Running MIGRATION_1_2")

        migratePersonsTable()
        migrateLocationsTable()
        migratePersonEncounterTable()
        migrateLocationVisitTable()
    }

    private val migratePersonsTable: SupportSQLiteDatabase.() -> Unit = {
        Timber.d("Table 'persons': Add column 'phoneNumber'")
        execSQL("ALTER TABLE `persons` ADD COLUMN `phoneNumber` TEXT")

        Timber.d("Table 'emailAddress': Add column 'phoneNumber'")
        execSQL("ALTER TABLE `persons` ADD COLUMN `emailAddress` TEXT")
    }

    private val migrateLocationsTable: SupportSQLiteDatabase.() -> Unit = {
        Timber.d("Table 'locations': Add column 'phoneNumber'")
        execSQL("ALTER TABLE `locations` ADD COLUMN `phoneNumber` TEXT")

        Timber.d("Table 'locations': Add column 'emailAddress'")
        execSQL("ALTER TABLE `locations` ADD COLUMN `emailAddress` TEXT")
    }

    private val migratePersonEncounterTable: SupportSQLiteDatabase.() -> Unit = {
        Timber.d("Table 'personencounters': Add column 'durationClassification'")
        execSQL("ALTER TABLE `personencounters` ADD COLUMN `durationClassification` TEXT")

        Timber.d("Table 'personencounters': Add column 'circumstances'")
        execSQL("ALTER TABLE `personencounters` ADD COLUMN `circumstances` TEXT")

        Timber.d("Table 'personencounters': Add column 'withMask'")
        execSQL("ALTER TABLE `personencounters` ADD COLUMN `withMask` INTEGER")

        Timber.d("Table 'personencounters': Add column 'wasOutside'")
        execSQL("ALTER TABLE `personencounters` ADD COLUMN `wasOutside` INTEGER")
    }

    private val migrateLocationVisitTable: SupportSQLiteDatabase.() -> Unit = {
        Timber.d("Table 'locationvisits': Add column 'duration'")
        execSQL("ALTER TABLE `locationvisits` ADD COLUMN `duration` INTEGER")

        Timber.d("Table 'locationvisits': Add column 'circumstances'")
        execSQL("ALTER TABLE `locationvisits` ADD COLUMN `circumstances` TEXT")
    }
}
