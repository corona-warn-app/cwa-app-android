package de.rki.coronawarnapp.contactdiary.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.migrations.SharedPreferencesMigration
import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettings
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences

class ContactDiaryStorageModuleTest : BaseTest() {

    @MockK lateinit var context: Context

    private lateinit var migration: SharedPreferencesMigration<ContactDiarySettings>
    private lateinit var sharedPrefs: SharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        sharedPrefs = MockSharedPreferences()
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs

        migration = ContactDiaryStorageModule.provideMigration(context)
    }

    @Test
    fun `migration success`() = runTest {}

    @Test
    fun `returns default if migration fails`() = runTest { }
}
