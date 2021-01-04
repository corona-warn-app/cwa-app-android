package de.rki.coronawarnapp.risk.storage

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.migrations.RiskResultDatabaseMigration1To2
import de.rki.coronawarnapp.risk.storage.internal.riskresults.PersistedRiskLevelResultDao
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTest
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
class RiskResultDatabaseMigrationTest : BaseTest() {
    private val DB_NAME = "riskresults_migration_test.db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RiskResultDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    /**
     * Test migration to create new primary key "monotonicId" column
     */
    @Test
    fun migrate1To2() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL(
                """
                    INSERT INTO "riskresults" (
                        "id",
                        "calculatedAt",
                        "totalRiskLevel",
                        "totalMinimumDistinctEncountersWithLowRisk",
                        "totalMinimumDistinctEncountersWithHighRisk",
                        "mostRecentDateWithLowRisk",
                        "mostRecentDateWithHighRisk",
                        "numberOfDaysWithLowRisk",
                        "numberOfDaysWithHighRisk"
                    ) VALUES (
                        '72c4084a-43a9-4fcf-86d4-36103bfbd492',
                        '2020-12-31T16:41:50.207Z',
                        '2',
                        '8',
                        '1',
                        '2020-12-29T16:41:50.038Z',
                        '2020-12-30T16:41:50.038Z',
                        '3',
                        '1'
                    );
                """.trimIndent()
            )
            execSQL(
                """
                    INSERT INTO "riskresults" (
                        "id",
                        "calculatedAt",
                        "totalRiskLevel",
                        "totalMinimumDistinctEncountersWithLowRisk",
                        "totalMinimumDistinctEncountersWithHighRisk",
                        "mostRecentDateWithLowRisk",
                        "mostRecentDateWithHighRisk",
                        "numberOfDaysWithLowRisk",
                        "numberOfDaysWithHighRisk"
                    ) VALUES (
                        '48a57f54-467b-4a0b-89c4-3c14e7ce65b5',
                        '2020-12-31T16:41:38.663Z',
                        '1',
                        '0',
                        '0',
                        NULL,
                        NULL,
                        '0',
                        '0'
                    );
                """.trimIndent()
            )
            execSQL(
                """
                    INSERT INTO "riskresults" (
                        "id",
                        "calculatedAt",
                        "failureReason"
                    ) VALUES (
                        '0235fef8-4332-4a43-b7d8-f5eacb54a6ee',
                        '2020-12-31T16:28:25.400Z',
                        'tracingOff'
                    );
                """.trimIndent()
            )

            close()
        }

        // Run migration
        helper.runMigrationsAndValidate(
            DB_NAME,
            2,
            true,
            RiskResultDatabaseMigration1To2
        )

        val daoDb = RiskResultDatabase.Factory(
            context = ApplicationProvider.getApplicationContext()
        ).create(databaseName = DB_NAME)

        val allEntries = runBlocking { daoDb.riskResults().allEntries().first() }

        allEntries.size shouldBe 3
        // Newest entry
        allEntries[0] shouldBe PersistedRiskLevelResultDao(
            monotonicId = 3,
            id = "0235fef8-4332-4a43-b7d8-f5eacb54a6ee",
            calculatedAt = Instant.parse("2020-12-31T16:28:25.400Z"),
            failureReason = RiskLevelResult.FailureReason.TRACING_OFF,
            aggregatedRiskResult = null
        )

        allEntries[1] shouldBe PersistedRiskLevelResultDao(
            monotonicId = 2,
            id = "48a57f54-467b-4a0b-89c4-3c14e7ce65b5",
            calculatedAt = Instant.parse("2020-12-31T16:41:38.663Z"),
            failureReason = null,
            aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
                totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.forNumber(
                    1
                ),
                totalMinimumDistinctEncountersWithLowRisk = 0,
                totalMinimumDistinctEncountersWithHighRisk = 0,
                mostRecentDateWithLowRisk = null,
                mostRecentDateWithHighRisk = null,
                numberOfDaysWithLowRisk = 0,
                numberOfDaysWithHighRisk = 0
            )
        )
        // Oldest entry, i.e. first one inserted
        allEntries[2] shouldBe PersistedRiskLevelResultDao(
            monotonicId = 1,
            id = "72c4084a-43a9-4fcf-86d4-36103bfbd492",
            calculatedAt = Instant.parse("2020-12-31T16:41:50.207Z"),
            failureReason = null,
            aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
                totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.forNumber(
                    2
                ),
                totalMinimumDistinctEncountersWithLowRisk = 8,
                totalMinimumDistinctEncountersWithHighRisk = 1,
                mostRecentDateWithLowRisk = Instant.parse("2020-12-29T16:41:50.038Z"),
                mostRecentDateWithHighRisk = Instant.parse("2020-12-30T16:41:50.038Z"),
                numberOfDaysWithLowRisk = 3,
                numberOfDaysWithHighRisk = 1
            )
        )
    }

    /**
     * If migration fails, drop the whole table and recreate it according to v2 schema
     */
    @Test
    fun migrate1To2_failure_drops_db() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL("DROP TABLE IF EXISTS riskresults")
            execSQL("CREATE TABLE IF NOT EXISTS `riskresults` (`id` TEXT NOT NULL, `calculatedAt` INTEGER, `failureReason` INTEGER)")
            execSQL("INSERT INTO `riskresults` (`id`, `calculatedAt`, `failureReason`) VALUES ('1', '2', '3')")

            close()
        }

        // Run migration
        helper.runMigrationsAndValidate(
            DB_NAME,
            2,
            true,
            RiskResultDatabaseMigration1To2
        )

        val daoDb = RiskResultDatabase.Factory(
            context = ApplicationProvider.getApplicationContext()
        ).create(databaseName = DB_NAME)

        val emptyResults = runBlocking { daoDb.riskResults().allEntries().first() }
        emptyResults.size shouldBe 0

        val expectedResult = PersistedRiskLevelResultDao(
            id = "48a57f54-467b-4a0b-89c4-3c14e7ce65b5",
            calculatedAt = Instant.parse("2020-12-31T16:41:38.663Z"),
            failureReason = null,
            aggregatedRiskResult = PersistedRiskLevelResultDao.PersistedAggregatedRiskResult(
                totalRiskLevel = RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.forNumber(
                    1
                ),
                totalMinimumDistinctEncountersWithLowRisk = 0,
                totalMinimumDistinctEncountersWithHighRisk = 0,
                mostRecentDateWithLowRisk = null,
                mostRecentDateWithHighRisk = null,
                numberOfDaysWithLowRisk = 0,
                numberOfDaysWithHighRisk = 0
            )
        )

        val insertedResult = runBlocking {
            daoDb.riskResults().insertEntry(expectedResult)
            daoDb.riskResults().allEntries().first().let {
                it.size shouldBe 1
                it.first()
            }
        }

        Timber.v("insertedResult=%s", insertedResult)
        insertedResult shouldBe expectedResult.copy(monotonicId = 1)
    }
}
