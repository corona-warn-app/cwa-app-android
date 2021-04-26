package de.rki.coronawarnapp.coronatest.antigen.profile

import android.content.Context
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences

internal class RATProfileSettingsTest : BaseTest() {
    @MockK lateinit var context: Context
    private val mockPreferences = MockSharedPreferences()
    private lateinit var ratProfileSettings: RATProfileSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every {
            context.getSharedPreferences("ratprofile_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences

        ratProfileSettings = RATProfileSettings(
            context,
            SerializationModule().baseGson()
        )
    }

    @Test
    fun getProfile() {
        val profile = RATProfile(
            firstName = "First name",
            lastName = "Last name",
            birthDate = "19800101",
            street = "Main street",
            zipCode = "12132",
            city = "London",
            phone = "111111111",
            email = "email@example.com"
        )
        ratProfileSettings.profile.update { profile }
        val json = (mockPreferences.dataMapPeek["ratprofile.settings.profile"] as String)
        json.toComparableJsonPretty() shouldBe
            """
                {
                  "firstName": "First name",
                  "lastName": "Last name",
                  "birthDate": "19800101",
                  "street": "Main street",
                  "zipCode": "12132",
                  "city": "London",
                  "phone": "111111111",
                  "email": "email@example.com"
                }
            """.trimIndent()
    }
}
