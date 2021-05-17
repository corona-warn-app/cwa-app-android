package de.rki.coronawarnapp.bugreporting.censors.vaccination

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CertificateQrCodeCensorTest {

    private val testRawString = "12340923042375893701389asf1283901830dfas"
    private val testCertificateData = VaccinationCertificateData(
        header = mockk(),
        certificate = VaccinationDGCV1(
            version = "1",
            nameData = VaccinationDGCV1.NameData(
                familyName = "Kevin",
                familyNameStandardized = "Kevin2",
                givenName = "Bob",
                givenNameStandardized = "Bob2"
            ),
            dob = "1969-11-16",
            vaccinationDatas = listOf(
                VaccinationDGCV1.VaccinationData(
                    targetId = "12345",
                    vaccineId = "1214765",
                    medicalProductId = "aaEd/easd",
                    marketAuthorizationHolderId = "ASD-2312",
                    doseNumber = 2,
                    totalSeriesOfDoses = 5,
                    dt = "1969-04-20",
                    countryOfVaccination = "DE",
                    certificateIssuer = "Herbert",
                    uniqueCertificateIdentifier = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
                )
            )
        )
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        CertificateQrCodeCensor.dataToCensor = CertificateQrCodeCensor.CensorData(
            rawString = null,
            certificateData = null,
        )
    }

    private fun createInstance() = CertificateQrCodeCensor()

    @Test
    fun `checkLog() should return censored LogLine`() = runBlockingTest {
        CertificateQrCodeCensor.dataToCensor = CertificateQrCodeCensor.CensorData(
            rawString = testRawString,
            certificateData = testCertificateData,
        )

        val censor = createInstance()

        val logLineToCensor = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Here comes the rawString: $testRawString of the vaccine certificate",
            tag = "I am tag",
            throwable = null
        )

        censor.checkLog(logLineToCensor) shouldBe logLineToCensor.copy(
            message = "Here comes the rawString: ########-####-####-####-########dfas of the vaccine certificate",
        )

        val certDataToCensor = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Hello my name is Kevin Bob, i was born at 1969-11-16, i have been " +
                "vaccinated with: 12345 1214765 aaEd/easd ASD-2312 1969-04-20 DE Herbert" +
                " urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
            tag = "I am tag",
            throwable = null
        )

        censor.checkLog(certDataToCensor) shouldBe certDataToCensor.copy(
            message = "Hello my name is nameData/familyName nameData/givenName, i was born at " +
                "vaccinationCertificate/dob, i have been vaccinated with: vaccinationData/targetId " +
                "vaccinationData/vaccineId vaccinationData/medicalProductId" +
                " vaccinationData/marketAuthorizationHolderId vaccinationData/dt" +
                " vaccinationData/countryOfVaccination vaccinationData/certificateIssuer" +
                " vaccinationData/uniqueCertificateIdentifier"
        )
    }

    @Test
    fun `checkLog() should return null if no data to censor was set`() = runBlockingTest {
        val censor = createInstance()

        val logLineNotToCensor = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Here comes the rawData: $testRawString",
            tag = "I am tag",
            throwable = null
        )

        censor.checkLog(logLineNotToCensor) shouldBe null
    }

    @Test
    fun `checkLog() should return null if nothing should be censored`() = runBlockingTest {
        CertificateQrCodeCensor.dataToCensor = CertificateQrCodeCensor.CensorData(
            rawString = testRawString.replace("1", "2"),
            certificateData = testCertificateData
        )

        val censor = createInstance()

        val logLineNotToCensor = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Here comes the rawString: $testRawString",
            tag = "I am tag",
            throwable = null
        )

        censor.checkLog(logLineNotToCensor) shouldBe null
    }
}
