package de.rki.coronawarnapp.profile.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import de.rki.coronawarnapp.profile.legacy.RATProfile
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProfileMigrationTest {

    @MockK lateinit var context: Context
    @MockK lateinit var settingsDataStore: ProfileSettingsDataStore
    @MockK lateinit var db: SupportSQLiteDatabase

    lateinit var factory: ProfileDatabase.Factory

    private val profile = RATProfile(
        firstName = "First name",
        lastName = "Last name",
        birthDate = LocalDate.parse("1950-08-01"),
        street = "Main street",
        zipCode = "12132",
        city = "London",
        phone = "111111111",
        email = "email@example.com"
    )

    private val emptyProfile = RATProfile(
        firstName = "",
        lastName = "",
        birthDate = null,
        street = "",
        zipCode = "",
        city = "London",
        phone = "",
        email = ""
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { settingsDataStore.deleteProfile() } returns Job()
        every { db.beginTransaction() } just Runs
        every { db.setTransactionSuccessful() } just Runs
        every { db.endTransaction() } just Runs
        every { db.insert(any(), any(), any()) } returns 1L
    }

    @Test
    fun `test migration`() = runBlocking {
        every { settingsDataStore.profileFlow } returns flowOf(profile)
        createInstance().migrateFromDataStore(db)
        verifySequence {
            settingsDataStore.profileFlow
            db.beginTransaction()
            db.insert(PROFILE_TABLE_NAME, SQLiteDatabase.CONFLICT_ABORT, any())
            db.setTransactionSuccessful()
            db.endTransaction()
            settingsDataStore.deleteProfile()
        }
    }

    @Test
    fun `test migration empty profile`() = runBlocking {
        every { settingsDataStore.profileFlow } returns flowOf(emptyProfile)
        createInstance().migrateFromDataStore(db)
        verifySequence {
            settingsDataStore.profileFlow
            db.beginTransaction()
            db.insert(PROFILE_TABLE_NAME, SQLiteDatabase.CONFLICT_ABORT, any())
            db.setTransactionSuccessful()
            db.endTransaction()
            settingsDataStore.deleteProfile()
        }
    }

    @Test
    fun `test migration no profile`() = runBlocking {
        every { settingsDataStore.profileFlow } returns flowOf(null)
        createInstance().migrateFromDataStore(db)
        verifySequence {
            settingsDataStore.profileFlow
        }
        verify(exactly = 0) {
            db.beginTransaction()
            db.insert(PROFILE_TABLE_NAME, SQLiteDatabase.CONFLICT_ABORT, any())
            db.setTransactionSuccessful()
            db.endTransaction()
            settingsDataStore.deleteProfile()
        }
    }

    private fun createInstance() = ProfileDatabase.Factory(
        context,
        TestCoroutineScope(),
        settingsDataStore
    )
}
