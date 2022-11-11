package de.rki.coronawarnapp.contactdiary.storage

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationVisitWrapper
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEncounterWrapper
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.internal.migrations.ContactDiaryDatabaseMigration1To2
import de.rki.coronawarnapp.contactdiary.storage.internal.migrations.ContactDiaryDatabaseMigration2To3
import de.rki.coronawarnapp.contactdiary.storage.internal.migrations.ContactDiaryDatabaseMigration3To4
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.ByteString.Companion.decodeBase64
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTestInstrumentation
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ContactDiaryDatabaseMigrationTest : BaseTestInstrumentation() {
    private val DB_NAME = "contactdiary_migration_test.db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ContactDiaryDatabase::class.java
    )

    @Before
    fun setup() {
        mockkObject(CoronaWarnApplication)
        mockkObject(AppInjector)
        every { AppInjector.component } returns mockk<ApplicationComponent>(relaxed = true)
            .apply {
                every { bugReporter } returns mockk(
                    relaxed = true
                )
            }
        every { CoronaWarnApplication.getAppContext() } returns ApplicationProvider.getApplicationContext()
    }

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
            emailAddress = null,
            traceLocationID = null
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
                circumstances = null,
                checkInID = null
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
    fun migrate2To3() {
        val location = ContactDiaryLocationEntity(
            locationId = 1,
            locationName = "Why do you want to have this information?",
            phoneNumber = "I'm not going to tell you yet",
            emailAddress = "I'mnotgoingtotell@you.yet",
            traceLocationID = null
        )

        val locationVisit = ContactDiaryLocationVisitEntity(
            id = 2,
            date = LocalDate.parse("2020-12-31"),
            fkLocationId = 1,
            duration = Duration.ofMinutes(13),
            circumstances = "That's none of your business",
            checkInID = null
        )

        val locationAfter = location.copy(traceLocationID = "jshrgu-aifhioaio-aofsjof-samofp-kjsadngsgf".decodeBase64())
        val locationVisitAfter = locationVisit.copy(checkInID = 101)

        val locationValues = ContentValues().apply {
            put("locationId", location.locationId)
            put("locationName", location.locationName)
            put("phoneNumber", location.phoneNumber)
            put("emailAddress", location.emailAddress)
        }

        val locationVisitValues = ContentValues().apply {
            put("id", locationVisit.id)
            put("date", locationVisit.date.toString())
            put("fkLocationId", locationVisit.fkLocationId)
            put("duration", locationVisit.duration?.toMillis())
            put("circumstances", locationVisit.circumstances)
        }

        helper.createDatabase(DB_NAME, 2).apply {
            insert("locations", SQLiteDatabase.CONFLICT_FAIL, locationValues)
            insert("locationvisits", SQLiteDatabase.CONFLICT_FAIL, locationVisitValues)

            close()
        }

        // Run migration
        helper.runMigrationsAndValidate(
            DB_NAME,
            3,
            true,
            ContactDiaryDatabaseMigration2To3
        )

        val daoDb = ContactDiaryDatabase.Factory(
            ctx = ApplicationProvider.getApplicationContext()
        ).create(databaseName = DB_NAME)

        runBlocking {
            daoDb.locationVisitDao().allEntries().first().single() shouldBe ContactDiaryLocationVisitWrapper(
                contactDiaryLocationEntity = location,
                contactDiaryLocationVisitEntity = locationVisit
            )

            // Test if new attributes are added correctly
            daoDb.locationDao().update(locationAfter)
            daoDb.locationVisitDao().update(locationVisitAfter)

            daoDb.locationVisitDao().allEntries().first().single() shouldBe ContactDiaryLocationVisitWrapper(
                contactDiaryLocationEntity = locationAfter,
                contactDiaryLocationVisitEntity = locationVisitAfter
            )
        }
    }

    @Test
    fun migrate3To4() {
        val location = ContactDiaryLocationEntity(
            locationId = 1,
            locationName = "My Location Name",
            phoneNumber = "1234567890",
            emailAddress = "email@address.com",
            traceLocationID = null
        )

        val locationVisit = ContactDiaryLocationVisitEntity(
            id = 2,
            date = LocalDate.parse("2020-12-31"),
            fkLocationId = 1,
            duration = Duration.ofMinutes(13),
            circumstances = "N/A",
            checkInID = null
        )

        val locationAfter = location.copy(traceLocationID = "jshrgu-aifhioaio-aofsjof-samofp-kjsadngsgf".decodeBase64())
        val locationVisitAfter = locationVisit.copy(checkInID = 101)

        val locationValues = ContentValues().apply {
            put("locationId", location.locationId)
            put("locationName", location.locationName)
            put("phoneNumber", location.phoneNumber)
            put("emailAddress", location.emailAddress)
        }

        val locationVisitValues = ContentValues().apply {
            put("id", locationVisit.id)
            put("date", locationVisit.date.toString())
            put("fkLocationId", locationVisit.fkLocationId)
            put("duration", locationVisit.duration?.toMillis())
            put("circumstances", locationVisit.circumstances)
        }

        helper.createDatabase(DB_NAME, 3).apply {
            insert("locations", SQLiteDatabase.CONFLICT_FAIL, locationValues)
            insert("locationvisits", SQLiteDatabase.CONFLICT_FAIL, locationVisitValues)
            close()
        }

        // Run migration
        helper.runMigrationsAndValidate(
            DB_NAME,
            4,
            true,
            ContactDiaryDatabaseMigration3To4
        )

        val daoDb = ContactDiaryDatabase.Factory(
            ctx = ApplicationProvider.getApplicationContext()
        ).create(databaseName = DB_NAME)

        runBlocking {
            daoDb.locationVisitDao().allEntries().first().single() shouldBe ContactDiaryLocationVisitWrapper(
                contactDiaryLocationEntity = location,
                contactDiaryLocationVisitEntity = locationVisit
            )

            // Test if new attributes are added correctly
            daoDb.locationDao().update(locationAfter)
            daoDb.locationVisitDao().update(locationVisitAfter)

            daoDb.locationVisitDao().allEntries().first().single() shouldBe ContactDiaryLocationVisitWrapper(
                contactDiaryLocationEntity = locationAfter,
                contactDiaryLocationVisitEntity = locationVisitAfter
            )
        }
    }

    @Test
    fun migrate4To5() {
        val location = ContactDiaryLocationEntity(
            locationId = 1,
            locationName = "My Location Name",
            phoneNumber = "1234567890",
            emailAddress = "email@address.com",
            traceLocationID = null
        )

        val locationVisit = ContactDiaryLocationVisitEntity(
            id = 2,
            date = LocalDate.parse("2020-12-31"),
            fkLocationId = 1,
            duration = Duration.ofMinutes(13),
            circumstances = "N/A",
            checkInID = null
        )

        val locationAfter = location.copy(traceLocationID = "jshrgu-aifhioaio-aofsjof-samofp-kjsadngsgf".decodeBase64())
        val locationVisitAfter = locationVisit.copy(checkInID = 101)

        val locationValues = ContentValues().apply {
            put("locationId", location.locationId)
            put("locationName", location.locationName)
            put("phoneNumber", location.phoneNumber)
            put("emailAddress", location.emailAddress)
        }

        val locationVisitValues = ContentValues().apply {
            put("id", locationVisit.id)
            put("date", locationVisit.date.toString())
            put("fkLocationId", locationVisit.fkLocationId)
            put("duration", locationVisit.duration?.toMillis())
            put("circumstances", locationVisit.circumstances)
        }

        val test = ContactDiaryCoronaTestEntity(
            id = "123-456-7890",
            testType = ContactDiaryCoronaTestEntity.TestType.PCR,
            result = ContactDiaryCoronaTestEntity.TestResult.POSITIVE,
            time = Instant.now()
        )
        val coronaTestValues = ContentValues().apply {
            put("id", test.id)
            put("testType", test.testType.raw)
            put("result", test.result.raw)
            put("time", test.time.toString())
        }

        helper.createDatabase(DB_NAME, 4).apply {
            insert("locations", SQLiteDatabase.CONFLICT_FAIL, locationValues)
            insert("locationvisits", SQLiteDatabase.CONFLICT_FAIL, locationVisitValues)
            insert("corona_tests", SQLiteDatabase.CONFLICT_FAIL, coronaTestValues)
            close()
        }

        // Run migration
        helper.runMigrationsAndValidate(
            DB_NAME,
            5,
            true
        )

        val daoDb = ContactDiaryDatabase.Factory(
            ctx = ApplicationProvider.getApplicationContext()
        ).create(databaseName = DB_NAME)

        runBlocking {
            daoDb.locationVisitDao().allEntries().first().single() shouldBe ContactDiaryLocationVisitWrapper(
                contactDiaryLocationEntity = location,
                contactDiaryLocationVisitEntity = locationVisit
            )

            // Test if new attributes are added correctly
            daoDb.locationDao().update(locationAfter)
            daoDb.locationVisitDao().update(locationVisitAfter)

            daoDb.locationVisitDao().allEntries().first().single() shouldBe ContactDiaryLocationVisitWrapper(
                contactDiaryLocationEntity = locationAfter,
                contactDiaryLocationVisitEntity = locationVisitAfter
            )

            daoDb.coronaTestDao().allTests().first().single() shouldBe test
            daoDb.submissionDao().allSubmissions().first().size shouldBe 0
        }
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
    fun migrate2To3_failure_throws_SQLiteException() {
        helper.createDatabase(DB_NAME, 2).apply {
            execSQL("DROP TABLE IF EXISTS locations")
            // Has incompatible existing column traceLocationID of wrong type
            execSQL("CREATE TABLE IF NOT EXISTS `locations` (`locationId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `locationName` TEXT NOT NULL, `phoneNumber` TEXT, `emailAddress` TEXT, `traceLocationID` INTEGER)")
            close()
        }

        shouldThrow<SQLiteException> {
            // Run migration
            helper.runMigrationsAndValidate(
                DB_NAME,
                3,
                true,
                ContactDiaryDatabaseMigration2To3
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
