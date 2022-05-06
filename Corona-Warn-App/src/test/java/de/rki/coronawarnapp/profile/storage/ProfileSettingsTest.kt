package de.rki.coronawarnapp.profile.storage

import android.content.Context
import de.rki.coronawarnapp.profile.legacy.RATProfile
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore

internal class ProfileSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    private val fakeDataStore = FakeDataStore()
    private lateinit var profileSettings: ProfileSettingsDataStore
    private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    private val profile = RATProfile(
        firstName = "First name",
        lastName = "Last name",
        birthDate = formatter.parseLocalDate("1950-08-01"),
        street = "Main street",
        zipCode = "12132",
        city = "London",
        phone = "111111111",
        email = "email@example.com"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        profileSettings = ProfileSettingsDataStore(
            { fakeDataStore },
            SerializationModule().baseGson(),
            TestCoroutineScope()
        )
    }

    @Test
    fun `Profile deletion`() {
        profileSettings.updateProfile(profile)
        profileSettings.deleteProfile()
        fakeDataStore[ProfileSettingsDataStore.PROFILE_KEY] shouldBe null
    }

    @Test
    fun `User on-boarding`() {
        fakeDataStore[ProfileSettingsDataStore.ONBOARDED_KEY] shouldBe null
        profileSettings.setOnboarded()
        fakeDataStore[ProfileSettingsDataStore.ONBOARDED_KEY] shouldBe true
    }

    @Test
    fun `Clear profile settings`() = runBlockingTest {
        profileSettings.updateProfile(profile)
        profileSettings.setOnboarded()
        profileSettings.reset()
        fakeDataStore[ProfileSettingsDataStore.ONBOARDED_KEY] shouldBe null
        fakeDataStore[ProfileSettingsDataStore.PROFILE_KEY] shouldBe null
    }
}
