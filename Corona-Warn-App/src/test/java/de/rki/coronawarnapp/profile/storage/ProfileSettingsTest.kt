package de.rki.coronawarnapp.profile.storage

import android.content.Context
import de.rki.coronawarnapp.profile.legacy.RATProfile
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeDataStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class ProfileSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    private val fakeDataStore = FakeDataStore()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val profile = RATProfile(
        firstName = "First name",
        lastName = "Last name",
        birthDate = LocalDate.parse("1950-08-01", formatter),
        street = "Main street",
        zipCode = "12132",
        city = "London",
        phone = "111111111",
        email = "email@example.com"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Profile deletion`() = runTest(UnconfinedTestDispatcher()) {
        val profileSettings = profileSettingsDataStore()
        profileSettings.updateProfile(profile)
        profileSettings.deleteProfile()
        fakeDataStore[ProfileSettingsDataStore.PROFILE_KEY] shouldBe null
    }

    @Test
    fun `User on-boarding`() = runTest(UnconfinedTestDispatcher()) {
        val profileSettings = profileSettingsDataStore()
        fakeDataStore[ProfileSettingsDataStore.ONBOARDED_KEY] shouldBe null
        profileSettings.setOnboarded()
        fakeDataStore[ProfileSettingsDataStore.ONBOARDED_KEY] shouldBe true
    }

    @Test
    fun `Clear profile settings`() = runTest(UnconfinedTestDispatcher()) {
        val profileSettings = profileSettingsDataStore()
        profileSettings.updateProfile(profile)
        profileSettings.setOnboarded()
        profileSettings.reset()
        fakeDataStore[ProfileSettingsDataStore.ONBOARDED_KEY] shouldBe null
        fakeDataStore[ProfileSettingsDataStore.PROFILE_KEY] shouldBe null
    }

    private fun TestScope.profileSettingsDataStore() = ProfileSettingsDataStore(
        dataStoreLazy = { fakeDataStore },
        gson = SerializationModule().baseGson(),
        appScope = this
    )
}
