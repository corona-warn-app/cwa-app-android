package de.rki.coronawarnapp.covidcertificate.person.core

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences

@Suppress("MaxLineLength")
class PersonCertificatesStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("certificate_person_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance() = PersonCertificatesSettings(
        context = context,
        mapper = SerializationModule.jacksonBaseMapper
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `clearing deletes all data`() {
        mockPreferences.edit {
            putString("deleteme", "test")
        }
        createInstance().clear()

        mockPreferences.dataMapPeek.keys.isEmpty() shouldBe true
    }

    @Test
    fun `store current cwa user person identifier`() {
        val testIdentifier = CertificatePersonIdentifier(
            firstNameStandardized = "firstname",
            lastNameStandardized = "lastname",
            dateOfBirthFormatted = "1999-12-24"
        )

        createInstance().apply {
            currentCwaUser.value shouldBe null
            currentCwaUser.update { testIdentifier }
            currentCwaUser.value shouldBe testIdentifier

            val raw = mockPreferences.dataMapPeek["certificate.person.current"] as String
            raw.toComparableJsonPretty() shouldBe """
                {
                  "dateOfBirth": "1999-12-24",
                  "familyNameStandardized": "lastname",
                  "givenNameStandardized": "firstname"
                }
            """.toComparableJsonPretty()
        }
    }
}
