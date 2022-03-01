package de.rki.coronawarnapp.bugreporting.censors.dcc

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.asRecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.recoveryCertificate1
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateTestData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

@Suppress("MaxLineLength")
class DccQrCodeCensorTest : BaseTest() {

    @Inject lateinit var certificateTestData: TestCertificateTestData

    private val vaccinationQrCode =
        "HC1:6BFOXN*TS0BI\$ZD.P9UOL97O4-2HH77HRM3DSPTLRR+%3.ZH9M9ESIGUBA KWML/O6HXK 0D+4O5VC9:BPCNYKMXEE1JAA/CZIK0JK1WL260X638J3-E3GG396B-43FZT-43:S0X37*ZV+FNI6HXY0ZSVILVQJF//05MVZJ5V.499TXY9KK9+OC+G9QJPNF67J6QW67KQY466PPM4MLJE+.PDB9L6Q2+PFQ5DB96PP5/P-59A%N+892 7J235II3NJ7PK7SLQMIJSBHVA7UJQWT.+S+ND%%M%331BH.IA.C8KRDL4O54O4IGUJKJGI0JAXD15IAXMFU*GSHGHD63DAOC9JU0H11+*4.\$S6ZC0JBZAB-C3QHISKE MCAOI8%M3V96-PY\$N6XOWLIBPIAYU:*JIRHUF2XZQ4H9 XJ72WG1K36VF/9BL56%E8T1OEEG%5TW5A 6YO67N6UCE:WT6BT-UMM:ABJK2TMDN1:FW-%T+\$D78NDSC3%5F61NYS-P9LOE0%J/ZAY:N5L4H-H/LH:AO3FU JHG7K46IOIMT.RE%PHLA21JRI3HTC\$AH"
    private val vaccinationCertificateData = DccData(
        header = mockk(),
        certificate = VaccinationDccV1(
            version = "1",
            nameData = DccV1.NameData(
                familyName = "Kevin",
                familyNameStandardized = "KEVIN",
                givenName = "Bob",
                givenNameStandardized = "BOB"
            ),
            dateOfBirthFormatted = "1969-11-16",
            dob = "1969-11-16",
            vaccination = DccV1.VaccinationData(
                targetId = "12345",
                vaccineId = "1214765",
                medicalProductId = "aaEd/easd",
                marketAuthorizationHolderId = "ASD-2312",
                doseNumber = 2,
                totalSeriesOfDoses = 5,
                dt = "1969-04-20",
                certificateCountry = "DE",
                certificateIssuer = "Herbert",
                uniqueCertificateIdentifier = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
            ),
            personIdentifier = CertificatePersonIdentifier(
                dateOfBirthFormatted = "1969-11-16",
                lastNameStandardized = "KEVIN",
                firstNameStandardized = "BOB"
            )
        ),
        certificateJson = "",
        kid = "",
        dscMessage = mockk()
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        runBlockingTest {
            DccQrCodeCensor.clearCertificateToCensor()
            DccQrCodeCensor.clearQRCodeStringToCensor()
        }
    }

    @AfterEach
    fun teardown() = runBlockingTest {
        DccQrCodeCensor.clearCertificateToCensor()
        DccQrCodeCensor.clearQRCodeStringToCensor()
    }

    private fun createInstance() = DccQrCodeCensor()

    @Test
    fun `censoring of vaccination certificate works`() = runBlockingTest {
        DccQrCodeCensor.addCertificateToCensor(vaccinationCertificateData)
        val censor = createInstance()
        val certDataToCensor = "Hello my name is Kevin Bob, i was born on 1969-11-16, i have been " +
            "vaccinated with: 12345 1214765 aaEd/easd ASD-2312 1969-04-20 DE Herbert" +
            " urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
        censor.checkLog(certDataToCensor)!!
            .compile()!!.censored shouldBe "Hello my name is nameData/familyName nameData/givenName, i was born on " +
            "dcc/dateOfBirth, i have been vaccinated with: vaccination/targetId " +
            "vaccination/vaccineId vaccination/medicalProductId" +
            " vaccination/marketAuthorizationHolderId vaccination/vaccinatedOnFormatted" +
            " vaccination/certificateCountry vaccination/certificateIssuer" +
            " vaccination/uniqueCertificateIdentifier"
    }

    @Test
    fun `checkLog() should return null if no data to censor was set`() = runBlockingTest {
        val censor = createInstance()
        val logLineNotToCensor = "Here comes the rawData: $vaccinationQrCode"
        censor.checkLog(logLineNotToCensor) shouldBe null
    }

    @Test
    fun `checkLog() should return null if nothing should be censored`() = runBlockingTest {
        DccQrCodeCensor.addQRCodeStringToCensor(vaccinationQrCode.replace("1", "2"))
        DccQrCodeCensor.addCertificateToCensor(vaccinationCertificateData)
        val censor = createInstance()
        val logLineNotToCensor = "Here comes the rawString: $vaccinationQrCode"
        censor.checkLog(logLineNotToCensor) shouldBe null
    }

    @Test
    fun `censoring of test certificate works`() = runBlockingTest {
        val data = certificateTestData.personATest1CertQRCode().data
        DccQrCodeCensor.addCertificateToCensor(data)
        val censor = createInstance()

        val certDataToCensor = with(data.certificate) {
            "Name is ${nameData.fullName}. Born $dateOfBirthFormatted. " +
                "Tested ${test.testResult} on ${test.sampleCollectedAtFormatted} at ${test.testCenter} " +
                "with ${test.testType} ${test.testName} from ${test.testNameAndManufacturer}"
        }

        censor.checkLog(certDataToCensor)!!
            .compile()!!.censored shouldBe "Name is nameData/givenName nameData/familyName. " +
            "Born dcc/dateOfBirth. Tested test/testResult on test/sampleCollectedAtFormatted at test/testCenter " +
            "with test/testType test/testName from test/testNameAndManufacturer"
    }

    @Test
    fun `censoring of recovery certificate works`() = runBlockingTest {
        val data = DccData(
            header = mockk(),
            recoveryCertificate1.asRecoveryCertificate!!,
            certificateJson = "",
            kid = "",
            dscMessage = mockk()
        )
        DccQrCodeCensor.addCertificateToCensor(data)
        val censor = createInstance()
        val certDataToCensor = with(data.certificate) {
            "Name is ${nameData.fullName}. Born $dateOfBirthFormatted. " +
                "Tested positive on ${recovery.testedPositiveOnFormatted}" +
                "valid from ${recovery.validFromFormatted} ${recovery.validFrom} " +
                "to ${recovery.validUntilFormatted} on ${recovery.validUntil}"
        }
        censor.checkLog(certDataToCensor)!!
            .compile()!!.censored shouldBe "Name is nameData/givenName nameData/familyName. Born dcc/dateOfBirth. " +
            "Tested positive on recovery/testedPositiveOnFormattedvalid " +
            "from recovery/validFromFormatted recovery/validFromFormatted " +
            "to recovery/validUntilFormatted on recovery/validUntilFormatted"
    }

    @Test
    fun `censoring of qr code works`() = runBlockingTest {
        DccQrCodeCensor.addQRCodeStringToCensor(vaccinationQrCode)
        val censor = createInstance()
        val logLineToCensor = "RawString: $vaccinationQrCode of certificate"
        censor.checkLog(logLineToCensor)!!
            .compile()!!.censored shouldBe "RawString: ###${vaccinationQrCode.takeLast(4)} of certificate"
    }

    @Test
    fun `EXPOSUREAPP-9075 - case`() = runBlockingTest {
        val censor = createInstance()
        val logLine =
            "2021-08-11T02:24:48.604Z V/DefaultExposureDetectio: Running timeout check (now=2021-08-11T02:24:48.603Z): [TrackedExposureDetection(identifier=3463faf6-6546-4f51-b9eb-85d45fa3af13, startedAt=2021-08-10T06:11:08.298Z, result=NO_MATCHES, finishedAt=2021-08-10T06:11:21.824Z, enfVersion=V2_WINDOW_MODE), TrackedExposureDetection(identifier=5b5b5d8c-353f-430e-8e9a-fea5312d3773, startedAt=2021-08-10T10:14:46.817Z, result=NO_MATCHES, finishedAt=2021-08-10T10:15:02.430Z, enfVersion=V2_WINDOW_MODE), TrackedExposureDetection(identifier=6ce6ea79-3fe6-44c8-b41d-1369ab9bf7de, startedAt=2021-08-10T15:17:58.451Z, result=NO_MATCHES, finishedAt=2021-08-10T15:18:13.911Z, enfVersion=V2_WINDOW_MODE), TrackedExposureDetection(identifier=0927fc48-7831-45a9-a48d-f19ba643841a, startedAt=2021-08-10T19:21:52.010Z, result=NO_MATCHES, finishedAt=2021-08-10T19:22:04.288Z, enfVersion=V2_WINDOW_MODE), TrackedExposureDetection(identifier=b6702fbe-22ce-430f-ad9f-0e05651d9243, startedAt=2021-08-10T23:24:45.592Z, result=NO_MATCHES, finishedAt=2021-08-10T23:25:01.560Z, enfVersion=V2_WINDOW_MODE)]"

        censor.checkLog(logLine) shouldBe null
    }
}
