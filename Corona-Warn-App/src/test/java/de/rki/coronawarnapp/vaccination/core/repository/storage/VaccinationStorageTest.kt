package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.vaccination.core.VaccinationTestData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `storing empty set deletes data`() {
        mockPreferences.edit {
            putString("dontdeleteme", "test")
            putString("vaccination.person.test", "test")
        }
        createInstance().personContainers = emptySet()

        mockPreferences.dataMapPeek.keys.single() shouldBe "dontdeleteme"
    }

    @Test
    fun `store one person`() {
        val instance = createInstance()
        instance.personContainers = setOf(VaccinationTestData.PERSON_C_DATA_1VAC_NOPROOF)

        val json =
            (mockPreferences.dataMapPeek["vaccination.person.1964-08-12#SCHMITT<MUSTERMANN#ERIKA<DOERTE"] as String)

        json.toComparableJsonPretty() shouldBe """
            {
                "vaccinationData": [
                    {
                        "vaccinationCertificateCOSE": "${VaccinationTestData.PERSON_C_VAC_1_COSE.data.base64()}",
                        "scannedAt": 1620062834471
                    }
                ],
                "proofData": [],
                "lastSuccessfulProofCertificateRun": 0,
                "proofCertificateRunPending": false
            }
        """.toComparableJsonPretty()

        instance.personContainers.single().apply {
            this shouldBe VaccinationTestData.PERSON_C_DATA_1VAC_NOPROOF
            this.vaccinations.single().vaccinationCertificateCOSE shouldBe VaccinationTestData.PERSON_C_VAC_1_COSE
            this.proofs shouldBe emptySet()
        }
    }
}
