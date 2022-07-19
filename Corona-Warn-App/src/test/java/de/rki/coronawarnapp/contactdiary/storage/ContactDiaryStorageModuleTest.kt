package de.rki.coronawarnapp.contactdiary.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.migrations.SharedPreferencesMigration
import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettings
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.matchers.shouldBe
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

    private val defaultContactDiarySettings = ContactDiarySettings()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        sharedPrefs = MockSharedPreferences()
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs

        migration = ContactDiaryStorageModule.provideMigration(context)
    }

    @Test
    fun `migration success`() = runTest {
        ContactDiarySettings.OnboardingStatus.values().forEach { status ->
            sharedPrefs.edit { putInt(LEGACY_ONBOARDING_STATUS_KEY, status.order) }
            migration.migrate(defaultContactDiarySettings) shouldBe ContactDiarySettings(status)
        }
    }

    @Test
    fun `returns default if migration fails`() = runTest {
        sharedPrefs.edit { putInt(LEGACY_ONBOARDING_STATUS_KEY, Int.MAX_VALUE) }

        shouldNotThrowAnyUnit {
            migration.migrate(defaultContactDiarySettings) shouldBe defaultContactDiarySettings
        }
    }
}
