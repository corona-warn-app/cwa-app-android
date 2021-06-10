package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.covidcertificate.vaccination.core.DaggerVaccinationTestComponent
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
        instance.personContainers = setOf(testData.personAData2Vac)

        val json =
            (mockPreferences.dataMapPeek["vaccination.person.1966-11-11#ASTRA<EINS#ANDREAS"] as String)

        json.toComparableJsonPretty() shouldBe """
            {
                "vaccinationData": [
                    {
                        "vaccinationQrCode": "${testData.personAVac1QRCodeString}",
                        "scannedAt": 1620062834471
                    }, {
                        "vaccinationQrCode": "${testData.personAVac2QRCodeString}",
                        "scannedAt": 1620069934471
                    }
                ]
            }
        """.toComparableJsonPretty()

        instance.personContainers.single().apply {
            this shouldBe testData.personAData2Vac
            this.vaccinations shouldBe setOf(
                testData.personAVac1Container,
                testData.personAVac2Container,
            )
        }
    }

    @Test
    fun `post processor injects data extractors`() {
        createInstance().personContainers = setOf(testData.personAData2Vac)

        createInstance().personContainers.single().vaccinations.first().qrCodeExtractor shouldNotBe null
    }
}
