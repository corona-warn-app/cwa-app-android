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
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTestInstrumentation

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
            hour = LocalTime.parse("23:00:00"),
            createdAt = Instant.ofEpochMilli(9999999),
        )
        helper.createDatabase(KeyCacheDatabase.DATABASE_NAME, 1).apply {
            execSQL(
                """
                    INSERT INTO "keyfiles" (
                        "type",
                        "location",
                        "day",
                        "hour",
                        "createdAt",
                        "checksumMD5",
                        "completed"
                    ) VALUES (
                        'country_day',
                        'DE',
                        '2020-08-23',
                        '23',
                        '9999999',
                        'etag',
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
