package de.rki.coronawarnapp.presencetracing.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.rki.coronawarnapp.presencetracing.storage.TraceLocationDatabase
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.presencetracing.storage.migration.PresenceTracingDatabaseMigration1To2
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.ByteString.Companion.encode
import java.time.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTestInstrumentation
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PresenceTracingDatabaseMigrationTest : BaseTestInstrumentation() {
    private val DB_NAME = "TraceLocations_test_db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TraceLocationDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2() {
        val locationIdBase64 = "41da2115-eba2-49bd-bf17-adb3d635ddaf".encode().base64()
        val cryptoGraphicSeed = "cryptographicSeed".encode().base64()
        val locationStart = Instant.parse("2021-01-01T12:30:00.000Z")
        val locationEnd = Instant.parse("2021-01-01T18:30:00.000Z")
        val checkInStart = Instant.parse("2021-01-01T14:30:00.000Z")
        val checkInEnd = Instant.parse("2021-01-01T16:30:00.000Z")
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL(
                """
                    INSERT INTO "checkin" (
                        "id",
                        "traceLocationIdBase64",
                        "version",
                        "type",
                        "description",
                        "address",
                        "traceLocationStart",
                        "traceLocationEnd",
                        "defaultCheckInLengthInMinutes",
                        "cryptographicSeedBase64",
                        "cnPublicKey",
                        "checkInStart",
                        "checkInEnd",
                        "completed",
                        "createJournalEntry",
                        "submitted"
                    ) VALUES (
                        '1',
                        '$locationIdBase64',
                        '1',
                        '2',
                        'brothers birthday',
                        'Malibu',
                        '$locationStart',
                        '$locationEnd',
                        '42',
                        '$cryptoGraphicSeed',
                        'cnPublicKey',
                        '$checkInStart',
                        '$checkInEnd',
                        '0',
                        '1',
                        '0'
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
            PresenceTracingDatabaseMigration1To2
        )

        val daoDb = TraceLocationDatabase.Factory(
            context = ApplicationProvider.getApplicationContext()
        ).create(databaseName = DB_NAME)

        val checkin = TraceLocationCheckInEntity(
            id = 1L,
            traceLocationIdBase64 = locationIdBase64,
            version = 1,
            type = 2,
            description = "brothers birthday",
            address = "Malibu",
            traceLocationStart = locationStart,
            traceLocationEnd = locationEnd,
            defaultCheckInLengthInMinutes = 42,
            cryptographicSeedBase64 = cryptoGraphicSeed,
            cnPublicKey = "cnPublicKey",
            checkInStart = checkInStart,
            checkInEnd = checkInEnd,
            completed = false,
            createJournalEntry = true,
            isSubmitted = false,
            hasSubmissionConsent = false,
        )
        runBlocking { daoDb.checkInDao().allEntries().first() }.single() shouldBe checkin
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        helper.createDatabase(DB_NAME, 1).apply {
            close()
        }

        // Open latest version of the database. Room will validate the schema once all migrations execute.
        TraceLocationDatabase.Factory(
            context = ApplicationProvider.getApplicationContext()
        ).create(databaseName = DB_NAME).apply {
            openHelper.writableDatabase
            close()
        }
    }
}
