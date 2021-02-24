package de.rki.coronawarnapp.contactdiary.storage

import android.database.sqlite.SQLiteException
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitWrapper
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterWrapper
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.internal.migrations.ContactDiaryDatabaseMigration1To2
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.joda.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTestInstrumentation
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ContactDiaryDatabaseMigrationTest : BaseTestInstrumentation() {
    private val DB_NAME = "contactdiary_migration_test.db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ContactDiaryDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    /**
     * Test migration to add new optional attributes
     */
    @Test
    fun migrate1To2() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL(
                """
                    INSERT INTO "locations" (
                        "locationId",
                        "locationName"
                    ) VALUES (
                        '1',
                        'Location1'
                    );
                """.trimIndent()
            )
            execSQL(
                """
                    INSERT INTO "persons" (
                        "personId",
                        "fullName"
                    ) VALUES (
                        '100',
                        'Person100'
                    );
                """.trimIndent()
            )
            execSQL(
                """
                    INSERT INTO "locationvisits" (
                        "id",
                        "date",
                        "fkLocationId"
                    ) VALUES (
                        '2',
                        '2020-04-20',
                        '1'
                    );
                """.trimIndent()
            )

            execSQL(
                """
                    INSERT INTO "personencounters" (
                        "id",
                        "date",
                        "fkPersonId"
                    ) VALUES (
                        '3',
                        '2020-12-31',
                        '100'
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
            ContactDiaryDatabaseMigration1To2
        )

        val daoDb = ContactDiaryDatabase.Factory(
            ctx = ApplicationProvider.getApplicationContext()
        ).create(databaseName = DB_NAME)

        val location = ContactDiaryLocationEntity(
            locationId = 1,
            locationName = "Location1",
            phoneNumber = null,
            emailAddress = null
        )
        runBlocking { daoDb.locationDao().allEntries().first() }.single() shouldBe location

        val person = ContactDiaryPersonEntity(
            personId = 100,
            fullName = "Person100",
            phoneNumber = null,
            emailAddress = null
        )
        runBlocking { daoDb.personDao().allEntries().first() }.single() shouldBe person

        runBlocking {
            daoDb.locationVisitDao().allEntries().first()
        }.single() shouldBe ContactDiaryLocationVisitWrapper(
            contactDiaryLocationEntity = location,
            contactDiaryLocationVisitEntity = ContactDiaryLocationVisitEntity(
                id = 2,
                date = LocalDate.parse("2020-04-20"),
                fkLocationId = 1,
                duration = null,
                circumstances = null
            )
        )

        runBlocking {
            daoDb.personEncounterDao().allEntries().first()
        }.single() shouldBe ContactDiaryPersonEncounterWrapper(
            contactDiaryPersonEntity = person,
            contactDiaryPersonEncounterEntity = ContactDiaryPersonEncounterEntity(
                id = 3,
                date = LocalDate.parse("2020-12-31"),
                fkPersonId = 100,
                withMask = null,
                wasOutside = null,
                durationClassification = null,
                circumstances = null
            )
        )
    }

    @Test
    fun migrate1To2_failure_throws() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL("DROP TABLE IF EXISTS locations")
            execSQL("DROP TABLE IF EXISTS locationvisits")
            execSQL("DROP TABLE IF EXISTS persons")
            execSQL("DROP TABLE IF EXISTS personencounters")
            // Has incompatible existing column phoneNumber of wrong type
            execSQL("CREATE TABLE IF NOT EXISTS `locations` (`locationId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `locationName` TEXT NOT NULL, `phoneNumber` INTEGER )")
            execSQL("CREATE TABLE IF NOT EXISTS `persons` (`personId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fullName` TEXT NOT NULL)")
            execSQL("CREATE TABLE IF NOT EXISTS `locationvisits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `fkLocationId` INTEGER NOT NULL, FOREIGN KEY(`fkLocationId`) REFERENCES `locations`(`locationId`) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED)")
            execSQL("CREATE TABLE IF NOT EXISTS `personencounters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `fkPersonId` INTEGER NOT NULL, FOREIGN KEY(`fkPersonId`) REFERENCES `persons`(`personId`) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED)")
            close()
        }

        shouldThrow<SQLiteException> {
            // Run migration
            helper.runMigrationsAndValidate(
                DB_NAME,
                2,
                true,
                ContactDiaryDatabaseMigration1To2
            )
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        helper.createDatabase(DB_NAME, 1).apply {
            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        ContactDiaryDatabase.Factory(
            ctx = ApplicationProvider.getApplicationContext()
        ).create(databaseName = DB_NAME).apply {
            openHelper.writableDatabase
            close()
        }
    }
}
