package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences
import javax.inject.Inject

@Suppress("MaxLineLength")
class VaccinationStorageTest : BaseTest() {

    @MockK lateinit var context: Context
    @Inject lateinit var testData: VaccinationTestData
    @Inject lateinit var qrCodeExtractor: DccQrCodeExtractor
    private lateinit var mockPreferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance() = VaccinationStorage(
        context = context,
        baseGson = SerializationModule().baseGson(),
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `storing empty set deletes data`() = runTest {
        mockPreferences.edit {
            putString("dontdeleteme", "test")
            putString("vaccination.certificate", "test")
        }
        createInstance().save(emptySet())

        mockPreferences.dataMapPeek.keys.single() shouldBe "dontdeleteme"
    }

    @Test
    fun `store one person`() {
        val vaccinationContainer2 = VaccinationTestData.personAVac2StoredCertificateData.copy(
            notifiedInvalidAt = Instant.ofEpochSecond(1234),
            notifiedBlockedAt = Instant.ofEpochSecond(1234),
            notifiedExpiredAt = Instant.ofEpochSecond(1234),
            notifiedExpiresSoonAt = Instant.ofEpochSecond(1234),
        )
        val personData = setOf(VaccinationTestData.personAVac1StoredCertificateData, vaccinationContainer2)
        runTest {
            val instance = createInstance()
            instance.save(personData)

            val json =
                (mockPreferences.dataMapPeek["vaccination.certificate"] as String)

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
                      "notifiedExpiresSoonAt": 1234000,
                      "notifiedExpiredAt": 1234000,
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
