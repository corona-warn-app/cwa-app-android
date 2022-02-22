package de.rki.coronawarnapp.covidcertificate.person.core

import android.content.Context
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.FakeDataStore

@Suppress("MaxLineLength")
class PersonCertificatesStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    private lateinit var mockPreferences: FakeDataStore
    private val personIdentifier = CertificatePersonIdentifier(
        dateOfBirthFormatted = "01.10.2020",
        firstNameStandardized = "fN",
        lastNameStandardized = "lN"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockPreferences = FakeDataStore()
    }

    private fun createInstance() = PersonCertificatesSettings(
        dataStore = mockPreferences,
        mapper = SerializationModule.jacksonBaseMapper,
        appScope = TestCoroutineScope(),
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `clearing deletes all data`() {

        createInstance().apply {
            setCurrentCwaUser(personIdentifier)
            setBoosterNotifiedAt(personIdentifier)
            clear()
        }

        mockPreferences[PersonCertificatesSettings.CURRENT_PERSON_KEY] shouldBe null
        mockPreferences[PersonCertificatesSettings.PERSONS_SETTINGS_MAP] shouldBe null
    }

    @Test
    fun `store current cwa user person identifier`() = runBlockingTest {
        val testIdentifier = CertificatePersonIdentifier(
            firstNameStandardized = "firstname",
            lastNameStandardized = "lastname",
            dateOfBirthFormatted = "1999-12-24"
        )

        createInstance().apply {
            currentCwaUser.first() shouldBe null
            setCurrentCwaUser(testIdentifier)
            currentCwaUser.first() shouldBe testIdentifier

            val raw = mockPreferences[PersonCertificatesSettings.CURRENT_PERSON_KEY] as String
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
