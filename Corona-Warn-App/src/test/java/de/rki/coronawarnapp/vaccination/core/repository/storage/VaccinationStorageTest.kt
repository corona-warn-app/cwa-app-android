package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.vaccination.core.ProofCertificate
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinationCertificate
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences

class VaccinationStorageTest : BaseTest() {

    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance() = VaccinationStorage(
        context = context,
        baseGson = SerializationModule().baseGson()
    )

    private val vaccinationCertificate1 = VaccinationCertificate(
        firstName = "",
        lastName =,
        dateOfBirth =,
        vaccinatedAt =,
        vaccineName =,
        vaccineManufacturer =,
        chargeId =,
        certificateIssuer =,
        certificateCountry =,
        certificateId =,
    )
    private val vaccinationCertificate2 = vaccinationCertificate1.copy(

    )
    private val proofCertificate = ProofCertificate(

    )
    private val vaccinatedPerson = VaccinatedPerson(
        vaccinationCertificates = setOf(vaccinationCertificate1, vaccinationCertificate2),
        proofCertificates = setOf(proofCertificate),
        lastUpdatedAt = Instant.ofEpochMilli(1234567L)
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `storing empty set deletes data`() {
        mockPreferences.edit {
            putString("dontdeleteme", "test")
            putString("vaccination.vaccinatedperson", "test")
        }
        createInstance().vaccinatedPersons = emptySet()

        mockPreferences.dataMapPeek.keys.single() shouldBe "dontdeleteme"
    }

    @Test
    fun `store one fully vaccinated person`() {
        val instance = createInstance()
        instance.vaccinatedPersons = setOf(
            vaccinatedPerson
        )

        val json = (mockPreferences.dataMapPeek["vaccination.vaccinatedperson"] as String)

        json.toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-pcr",
                    "registeredAt": 1000,
                }
            ]
        """.toComparableJsonPretty()

        instance.vaccinatedPersons.single().apply {
            this shouldBe vaccinatedPerson.copy(
                isUpdatingData = false
            )
            identifier shouldBe ""
        }
    }

    @Test
    fun `store incompletely vaccinated person`() {
        TODO()
    }
}
