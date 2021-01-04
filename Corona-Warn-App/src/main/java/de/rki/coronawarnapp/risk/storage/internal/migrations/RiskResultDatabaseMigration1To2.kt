package de.rki.coronawarnapp.risk.storage.internal.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import timber.log.Timber

/**
 * Migrates the RiskResultDataBase from schema version 1 to schema version 2
 * The primary key column "id" was replaced with the newly added "monotonicId" column
 * This was done to allow the app to determine the "latest" calculation independent of the calculation timestamp,
 * which could be problematic if timetravel happens.
 */
object RiskResultDatabaseMigration1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Timber.i("Attempting migration 1->2...")
            performMigration(database)
            Timber.i("Migration 1->2 successful.")
        } catch (e: Exception) {
            Timber.e(e, "Migration 1->2 failed, dropping tables...")
            e.report(ExceptionCategory.INTERNAL, "RiskResult database migration failed.")

            try {
                recreateRiskResults(database)
            } catch (e: Exception) {
                e.report(ExceptionCategory.INTERNAL, "Migration failed, table recreation failed too!")
                throw e
            }

            Timber.w("Migration failed, but fallback via reset was successful.")
        }
    }

    private fun performMigration(database: SupportSQLiteDatabase) = with(database) {
        Timber.i("Running MIGRATION_1_2: Create new table.")
        execSQL(
            """
                CREATE TABLE IF NOT EXISTS `riskresults_new` (
                    `monotonicId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `id` TEXT NOT NULL,
                    `calculatedAt` TEXT NOT NULL,
                    `failureReason` TEXT,
                    `totalRiskLevel` INTEGER,
                    `totalMinimumDistinctEncountersWithLowRisk` INTEGER,
                    `totalMinimumDistinctEncountersWithHighRisk` INTEGER,
                    `mostRecentDateWithLowRisk` TEXT,
                    `mostRecentDateWithHighRisk` TEXT,
                    `numberOfDaysWithLowRisk` INTEGER,
                    `numberOfDaysWithHighRisk` INTEGER
                )
            """.trimIndent()
        )

        Timber.i("Running MIGRATION_1_2: Insert old data.")
        execSQL(
            """
                INSERT INTO riskresults_new(
                    id,
                    calculatedAt,
                    failureReason,
                    totalRiskLevel,
                    totalMinimumDistinctEncountersWithLowRisk,
                    totalMinimumDistinctEncountersWithHighRisk,
                    mostRecentDateWithLowRisk,
                    mostRecentDateWithHighRisk,
                    numberOfDaysWithLowRisk,
                    numberOfDaysWithHighRisk
                ) SELECT
                    id,
                    calculatedAt,
                    failureReason,
                    totalRiskLevel,
                    totalMinimumDistinctEncountersWithLowRisk,
                    totalMinimumDistinctEncountersWithHighRisk,
                    mostRecentDateWithLowRisk,
                    mostRecentDateWithHighRisk,
                    numberOfDaysWithLowRisk,
                    numberOfDaysWithHighRisk
                FROM riskresults
            """.trimIndent()
        )

        Timber.i("Running MIGRATION_1_2: Drop old table.")
        execSQL("DROP TABLE riskresults")

        Timber.i("Running MIGRATION_1_2: Rename temporary table.")
        execSQL("ALTER TABLE riskresults_new RENAME TO riskresults")
    }

    private fun recreateRiskResults(database: SupportSQLiteDatabase) = with(database) {
        Timber.i("Dropping and creating new riskResults v2 table.")

        execSQL("DROP TABLE IF EXISTS riskresults")
        execSQL("DROP TABLE IF EXISTS riskresults_new")

        execSQL(
            """
                CREATE TABLE `riskresults` (
                    `monotonicId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `id` TEXT NOT NULL,
                    `calculatedAt` TEXT NOT NULL,
                    `failureReason` TEXT,
                    `totalRiskLevel` INTEGER,
                    `totalMinimumDistinctEncountersWithLowRisk` INTEGER,
                    `totalMinimumDistinctEncountersWithHighRisk` INTEGER,
                    `mostRecentDateWithLowRisk` TEXT,
                    `mostRecentDateWithHighRisk` TEXT,
                    `numberOfDaysWithLowRisk` INTEGER,
                    `numberOfDaysWithHighRisk` INTEGER
                )
            """.trimIndent()
        )
    }
}
