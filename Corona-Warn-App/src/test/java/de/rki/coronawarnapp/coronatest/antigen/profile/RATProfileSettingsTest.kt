package de.rki.coronawarnapp.coronatest.antigen.profile

import android.content.Context
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.TestCoroutineScope
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences

internal class RATProfileSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    private val mockPreferences = MockSharedPreferences()
    private lateinit var ratProfileSettings: RATProfileSettingsDataStore
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

        every {
            context.getSharedPreferences("ratprofile_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences

        ratProfileSettings = RATProfileSettingsDataStore(
            context,
            SerializationModule().baseGson(),
            TestCoroutineScope()
        )
    }

    @Test
    fun `Profile has birth date`() {
        ratProfileSettings.updateProfile(profile)
        val json = (mockPreferences.dataMapPeek["ratprofile.settings.profile"] as String)
        json.toComparableJsonPretty() shouldBe
            """
                {
                  "firstName": "First name",
                  "lastName": "Last name",
                  "birthDate": "1950-08-01",
                  "street": "Main street",
                  "zipCode": "12132",
                  "city": "London",
                  "phone": "111111111",
                  "email": "email@example.com"
                }
            """.trimIndent()
    }

    @Test
    fun `Profile hasn't birth date`() {
        ratProfileSettings.updateProfile(profile.copy(birthDate = null))
        val json = (mockPreferences.dataMapPeek["ratprofile.settings.profile"] as String)
        json.toComparableJsonPretty() shouldBe
            """
                {
                  "firstName": "First name",
                  "lastName": "Last name",
                  "street": "Main street",
                  "zipCode": "12132",
                  "city": "London",
                  "phone": "111111111",
                  "email": "email@example.com"
                }
            """.trimIndent()
    }

    @Test
    fun `Profile has empty properties`() {
        ratProfileSettings.updateProfile(profile.copy(firstName = "", lastName = ""))
        val json = (mockPreferences.dataMapPeek["ratprofile.settings.profile"] as String)
        json.toComparableJsonPretty() shouldBe
            """
                {
                  "firstName": "",
                  "lastName": "",
                  "birthDate": "1950-08-01",
                  "street": "Main street",
                  "zipCode": "12132",
                  "city": "London",
                  "phone": "111111111",
                  "email": "email@example.com"
                }
            """.trimIndent()
    }
}
