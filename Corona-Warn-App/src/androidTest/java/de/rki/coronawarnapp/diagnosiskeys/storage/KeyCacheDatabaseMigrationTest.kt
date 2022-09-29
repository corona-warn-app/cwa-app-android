package de.rki.coronawarnapp.diagnosiskeys.storage

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTestInstrumentation
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class KeyCacheDatabaseMigrationTest : BaseTestInstrumentation() {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        KeyCacheDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    /**
     * Test migration to add new optional attributes
     */
    @Test
    fun migrate1To2() {
        val keyHour = CachedKeyInfo(
            type = CachedKeyInfo.Type.LOCATION_DAY,
            location = LocationCode("DE"),
            day = LocalDate.parse("2020-08-23"),
            hour = LocalTime.parse("12:00:00"),
            createdAt = Instant.parse("2020-08-23T12:46:39.999Z"),
        )
        helper.createDatabase(KeyCacheDatabase.DATABASE_NAME, 1).apply {
            execSQL(
                """
                    INSERT INTO "keyfiles" (
                        "id",
                        "type",
                        "location",
                        "day",
                        "hour",
                        "createdAt",
                        "completed"
                    ) VALUES (
                        '0f3e9205ba2c753afc5772e9d40b10dded89666d',
                        'country_day',
                        'DE',
                        '2020-08-23',
                        '12:00:00',
                        '2020-08-23T12:46:39.999Z',
                        '0'
                    );
                """.trimIndent()
            )
            close()
        }

        // Run migration
        helper.runMigrationsAndValidate(
            KeyCacheDatabase.DATABASE_NAME,
            2,
            true,
            KeyCacheDatabaseMigration1To2
        )

        val daoDb = KeyCacheDatabase.Factory(
            context = ApplicationProvider.getApplicationContext()
        ).create()

        runBlocking { daoDb.cachedKeyFiles().allEntries().first() }.single() shouldBe keyHour
    }
}
