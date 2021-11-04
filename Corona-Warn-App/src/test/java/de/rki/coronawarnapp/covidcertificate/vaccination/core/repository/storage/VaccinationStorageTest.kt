package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences
import javax.inject.Inject

@Suppress("MaxLineLength")
class VaccinationStorageTest : BaseTest() {

    @MockK lateinit var context: Context
    @Inject lateinit var postProcessor: ContainerPostProcessor
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
        containerPostProcessor = postProcessor,
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `storing empty set deletes data`() = runBlockingTest {
        mockPreferences.edit {
            putString("dontdeleteme", "test")
            putString("vaccination.person.test", "test")
        }
        createInstance().save(emptySet())

        mockPreferences.dataMapPeek.keys.single() shouldBe "dontdeleteme"
    }

    @Test
    fun `store one person`() = runBlockingTest {
        val instance = createInstance()
        instance.save(setOf(testData.personAData2Vac))

        val json =
            (mockPreferences.dataMapPeek["vaccination.person.1966-11-11#ASTRA<EINS#ANDREAS"] as String)

        json.toComparableJsonPretty() shouldBe """
            {
                "vaccinationData": [
                    {
                        "vaccinationQrCode": "${testData.personAVac1QRCodeString}",
                        "scannedAt": 1620062834471,
                        "lastSeenStateChange": {
                            "expiresAt": 1620062834471,
                            "type": "ExpiringSoon"
                        },
                        "lastSeenStateChangeAt": 1620062834471,
                        "certificateSeenByUser": true
                    }, {
                        "vaccinationQrCode": "${testData.personAVac2QRCodeString}",
                        "scannedAt": 1620069934471,
                        "certificateSeenByUser": true
                    }
                ]
            }
        """.toComparableJsonPretty()

        instance.load().single().apply {
            this shouldBe testData.personAData2Vac
            this.vaccinations shouldBe setOf(
                testData.personAVac1Container,
                testData.personAVac2Container,
            )
        }
    }

    @Test
    fun `post processor injects data extractors`() = runBlockingTest {
        createInstance().save(setOf(testData.personAData2Vac))

        createInstance().load().single().vaccinations.first().qrCodeExtractor shouldNotBe null
    }

    @Test
    fun `data migration no changes`() = runBlockingTest {
        val instance = createInstance()
        instance.save(setOf(testData.personAData2Vac))
        instance.load() shouldBe setOf(testData.personAData2Vac)
    }

    @Test
    fun `data migration regroup`() = runBlockingTest {
        val qrCode =
            "HC1:NCFOXN%TSMAHN-H3ZSUZK+.V0ET9%6-AH+TA1ROR\$SIOO+.I%NP20J1F5L1WV0MNO4*J8OX4W\$C2VLWLI+J53O8J.V J8\$XJK*L5R1ZP3YXL.T1 R1VW5G H1R5GQ3GYBKUBFX9KS5W0SRZJ3T9NYJQP5K+HI-J:PIWEG%*47%S%*48YIZ73423ZQTW63-E32R44$28A9H0D3ZCL4JMYAZ+S-A5\$XKX6T2YC 35H/ITX8GL2/IE%TE6UG+ZEAT1HQ10IGSU9ZIEQKERQ8IY1I\$HH%U8 9PS5-LH/CJTK96L6SR9MU9DV5 R13PI%F1PN1/T1%%HN9GL\$UP\$I/XK\$M8CL6PZB*L8PK99Q9E\$BDZIX7JMESLV6 GUPS0TCRV NC+GC.ULR9CBPPZII+QLKC3-SY\$NMSB3CP-9IN:8OK3U3A49CM.S-YNIEK96K0DIEM4C3BDKBYLDR4DM1D6MS3FUHE68FL8LNKY4-/MK:4E2DZ7W6DFQWL8RE*CRL D2ESS87\$PVYK7+8M+0C5/TD8SDISV%GS6E4ZKRXNI:M\$FW9LP\$MVSMCH4M+\$T610BEM:4"

        val personADataContainer = VaccinationCertificateQRCode(
            qrCode,
            qrCodeExtractor.extract(qrCode, DccV1Parser.Mode.CERT_SINGLE_STRICT).data as DccData<VaccinationDccV1>
        ).toVaccinationContainer(
            Instant.EPOCH,
            qrCodeExtractor,
            false
        )

        val personAData1VacFuzzyKey = VaccinatedPersonData(
            vaccinations = setOf(personADataContainer)
        )
        val instance = createInstance()
        instance.save(setOf(testData.personAData2Vac, testData.personBData1Vac, personAData1VacFuzzyKey))
        val result = instance.load()
        result.size shouldBeExactly 2
        result.find { it.identifier == testData.personAData2Vac.identifier }!!.vaccinations.size shouldBeExactly 3
        result.find { it.identifier == testData.personBData1Vac.identifier }!!.vaccinations.size shouldBeExactly 1
        result.count { it.identifier == testData.personBData1Vac.identifier } shouldBeExactly 1
        result.count { it.identifier == testData.personAData2Vac.identifier } shouldBeExactly 1
    }
}
