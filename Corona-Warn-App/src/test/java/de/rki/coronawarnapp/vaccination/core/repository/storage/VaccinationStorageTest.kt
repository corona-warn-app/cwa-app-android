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
            (mockPreferences.dataMapPeek["vaccination.person.1966-11-11#ASTRA<EINS#ANDREAS"] as String)

        json.toComparableJsonPretty() shouldBe """
            {
                "vaccinationData": [
                    {
                        "vaccinationCertificateCOSE": "${VaccinationTestData.PERSON_A_VAC_1_COSE.data.base64()}",
                        "scannedAt": 1620062834471
                    }, {
                        "vaccinationCertificateCOSE": "${VaccinationTestData.PERSON_A_VAC_2_COSE.data.base64()}",
                        "scannedAt": 1620069934471
                    }
                ],
                "proofData": [
                    {
                        "proofCertificateCOSE": "0oRDoQEmoQRQqs76QaMRQrC+bjTS2a3mSFkBK6QBYkRFBBpgo+nnBhpgmk2wOQEDoQGkYXaBqmJjaXgxMDFERS8wMDAwMS8xMTE5MzA1MDA1LzZJUFlCQUlEV0VXUldXNzNRRVA5MkZRU04jU2Jjb2JERWJkbgJiZHRqMjAyMS0wNC0yN2Jpc3gqQnVuZGVzbWluaXN0ZXJpdW0gZsO8ciBHZXN1bmRoZWl0IC0gVGVzdDAxYm1hbU9SRy0xMDAwMDE2OTlibXBsRVUvMS8yMS8xNTI5YnNkAmJ0Z2k4NDA1MzkwMDZidnBqMTExOTMwNTAwNWNkb2JqMTk2Ni0xMS0xMWNuYW2kYmZua0FzdHLDoSBFaW5zYmduZ0FuZHJlYXNjZm50akFTVFJBPEVJTlNjZ250Z0FORFJFQVNjdmVyZTEuMC4wWEC+Y2lLfL80dTSNr6McGcjQw6thEA9CTWF/doSUJh0B728ktjaCt40kn9ABTfuh/WYTdDqzWe7DFFGz7VhNbBm0",
                        "receivedAt": 1620062839471
                    }
                ],
                "lastSuccessfulProofCertificateRun": 0,
                "proofCertificateRunPending": false
            }
        """.toComparableJsonPretty()

        instance.personContainers.single().apply {
            this shouldBe VaccinationTestData.PERSON_C_DATA_1VAC_NOPROOF
            this.vaccinations shouldBe setOf(
                VaccinationTestData.PERSON_A_VAC_1_CONTAINER,
                VaccinationTestData.PERSON_A_VAC_2_CONTAINER,
            )
            this.proofs shouldBe setOf(VaccinationTestData.PERSON_A_PROOF_CONTAINER)
        }
    }
}
