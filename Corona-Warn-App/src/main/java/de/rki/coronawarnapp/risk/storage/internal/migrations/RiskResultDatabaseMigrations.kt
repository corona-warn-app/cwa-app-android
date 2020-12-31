package de.rki.coronawarnapp.risk.storage.internal.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

object RiskResultDatabaseMigrations {
    /**
     * Migrates the RiskResultDataBase from schema version 1 to schema version 2
     * The primary key column "id" was replaced with the newly added "monotonicId" column
     * This was done to allow the app to determine the "latest" calculation independent of the calculation timestamp,
     * which could be problematic if timetravel happens.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            Timber.i("Running MIGRATION_1_2: Create new table.")
            database.execSQL(
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
            database.execSQL(
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
            database.execSQL("DROP TABLE riskresults")

            Timber.i("Running MIGRATION_1_2: Rename temporary table.")
            database.execSQL("ALTER TABLE riskresults_new RENAME TO riskresults")
        }
    }
}
