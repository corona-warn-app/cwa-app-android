package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData.Companion.personAData2Vac
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage.Companion.PKEY_VACCINATION_CERT
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.FakeDataStore
import java.time.Instant
import javax.inject.Inject

@Suppress("MaxLineLength")
class VaccinationStorageTest : BaseTest() {

    @Inject lateinit var testData: VaccinationTestData
    @Inject lateinit var qrCodeExtractor: DccQrCodeExtractor
    private lateinit var dataStore: FakeDataStore

    private val gson = SerializationModule().baseGson()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        dataStore = FakeDataStore()
    }

    private fun createInstance() = VaccinationStorage(
        dataStore = dataStore
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `legacy data is correctly loaded`() = runTest2 {
        val storage = createInstance()
        val json = gson.toJson(personAData2Vac)
        dataStore[stringPreferencesKey("vaccination.person.person1")] = json

        val legacyData = storage.loadLegacyData()
        legacyData shouldNotBe emptySet<VaccinatedPersonData>()
        legacyData.contains(personAData2Vac) shouldBe true
    }

    @Test
    fun `legacy data is empty when key doesn't match`() = runTest2 {
        val storage = createInstance()
        val json = gson.toJson(personAData2Vac)
        dataStore[stringPreferencesKey("vaccination.animal")] = json

        storage.loadLegacyData() shouldBe emptySet<VaccinatedPersonData>()
    }

    @Test
    fun `storing empty set deletes data`() = runTest {
        dataStore[stringPreferencesKey("dontdeleteme")] = "test"
        dataStore[PKEY_VACCINATION_CERT] = "test"

        createInstance().save(emptySet())

        dataStore[stringPreferencesKey("dontdeleteme")] shouldBe "test"
    }

    @Test
    fun `store one person`() {
        val vaccinationContainer2 = VaccinationTestData.personAVac2StoredCertificateData.copy(
            notifiedInvalidAt = Instant.ofEpochSecond(1234),
            notifiedBlockedAt = Instant.ofEpochSecond(1234),
        )
        val personData = setOf(VaccinationTestData.personAVac1StoredCertificateData, vaccinationContainer2)
        runTest {
            val instance = createInstance()
            instance.save(personData)

            val json = (dataStore[PKEY_VACCINATION_CERT] as String)

            json.toComparableJsonPretty() shouldBe """
                [
                    {
                      "vaccinationQrCode": "${VaccinationTestData.personAVac1QRCodeString}",
                      "scannedAt": 1620062834471,
                      "lastSeenStateChange": {
                        "expiresAt": 1620062834471,
                        "type": "ExpiringSoon"
                      },
                      "lastSeenStateChangeAt": 1620062834471,
                      "certificateSeenByUser": true
                    },
                    {
                      "vaccinationQrCode": "${VaccinationTestData.personAVac2QRCodeString}",
                      "scannedAt": 1620069934471,
                      "notifiedInvalidAt": 1234000,
                      "notifiedBlockedAt": 1234000,
                      "certificateSeenByUser": true
                    }
                  ]
            """.toComparableJsonPretty()

            instance.load().apply {
                this shouldBe personData
            }
        }
    }
}
