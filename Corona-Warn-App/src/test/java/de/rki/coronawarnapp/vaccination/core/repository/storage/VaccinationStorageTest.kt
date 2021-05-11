package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.vaccination.core.DaggerVaccinationTestComponent
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
import javax.inject.Inject

class VaccinationStorageTest : BaseTest() {

    @MockK lateinit var context: Context
    @Inject lateinit var postProcessor: ContainerPostProcessor
    @Inject lateinit var testData: VaccinationTestData
    private lateinit var mockPreferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerVaccinationTestComponent.factory().create().inject(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance() = VaccinationStorage(
        context = context,
        baseGson = SerializationModule().baseGson(),
        containerPostProcessor = postProcessor,
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
        instance.personContainers = setOf(testData.personAData2Vac1Proof)

        val json =
            (mockPreferences.dataMapPeek["vaccination.person.1966-11-11#ASTRA<EINS#ANDREAS"] as String)

        json.toComparableJsonPretty() shouldBe """
            {
                "vaccinationData": [
                    {
                        "vaccinationCertificateCOSE": "${testData.personAVac1COSE.data.base64()}",
                        "scannedAt": 1620062834471
                    }, {
                        "vaccinationCertificateCOSE": "${testData.personAVac2COSE.data.base64()}",
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
            this shouldBe testData.personAData2Vac1Proof
            this.vaccinations shouldBe setOf(
                testData.personAVac1Container,
                testData.personAVac2Container,
            )
            this.proofs shouldBe setOf(testData.personAProof1Container)
        }
    }
}
