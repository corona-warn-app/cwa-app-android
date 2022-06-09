package de.rki.coronawarnapp.covidcertificate.pdf.core

import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class CertificateDataInjectorTest : BaseTest() {

    @BeforeEach
    fun setup() {
        mockkStatic("de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.helper.CertificateDataInjectorKt")
    }

    @Test
    fun `test VC injector`() {
        val vc = mockk<VaccinationCertificate>().apply {
            every { fullNameFormatted } returns "Full Name"
            every { dateOfBirthFormatted } returns "1990-10-10"
            every { uniqueCertificateIdentifier } returns "UNIQUE_CERTIFICATE_IDENTIFIER"
            every { targetDisease } returns "Covid 19"
            every { certificateCountry } returns "Germany"
            every { rawCertificate.payload.certificateCountry } returns "DE"
            every { qrCodeBase64() } returns "1234"
            every { certificateIssuer } returns "Robert Koch"
            every { medicalProductName } returns "mRNa"
            every { vaccineManufacturer } returns "BionTech"
            every { doseNumber } returns 1
            every { totalSeriesOfDoses } returns 2
            every { vaccinatedOnFormatted } returns "2020-10-12"
            every { vaccineTypeName } returns "Astra"
        }

        template(fileName = "template/de_vc_v4.1.svg").inject(vc).contains("\$") shouldBe false
        template(fileName = "template/vc_v4.1.svg").inject(vc).contains("\$") shouldBe false
    }

    @Test
    fun `test RC injector`() {
        val rc = mockk<RecoveryCertificate>().apply {
            every { fullNameFormatted } returns "Full Name"
            every { dateOfBirthFormatted } returns "1990-10-10"
            every { uniqueCertificateIdentifier } returns "UNIQUE_CERTIFICATE_IDENTIFIER"
            every { targetDisease } returns "Covid 19"
            every { certificateCountry } returns "Germany"
            every { rawCertificate.payload.certificateCountry } returns "DE"
            every { qrCodeBase64() } returns "1234"
            every { certificateIssuer } returns "Robert Koch"
            every { validFromFormatted } returns "2020-12-10"
            every { validUntilFormatted } returns "2021-12-10"
            every { testedPositiveOnFormatted } returns "2021-11-10"
        }

        template(fileName = "template/de_rc_v4.1.svg").inject(rc).contains("\$") shouldBe false
        template(fileName = "template/rc_v4.1.svg").inject(rc).contains("\$") shouldBe false
    }

    @Test
    fun `test TC injector`() {
        val tc = mockk<TestCertificate>().apply {
            every { fullNameFormatted } returns "Full Name"
            every { dateOfBirthFormatted } returns "1990-10-10"
            every { uniqueCertificateIdentifier } returns "UNIQUE_CERTIFICATE_IDENTIFIER"
            every { targetDisease } returns "Covid 19"
            every { certificateCountry } returns "Germany"
            every { rawCertificate.payload.certificateCountry } returns "DE"
            every { qrCodeBase64() } returns "1234"
            every { certificateIssuer } returns "Robert Koch"
            every { testName } returns "Rapid"
            every { testNameAndManufacturer } returns "Robert Koch"
            every { sampleCollectedAtFormatted } returns "2020-10-20"
            every { testResult } returns "Negative"
            every { testCenter } returns "TestIQ"
            every { testType } returns "Rapid"
        }

        template(fileName = "template/de_tc_v4.1.svg").inject(tc).contains("\$") shouldBe false
        template(fileName = "template/tc_v4.1.svg").inject(tc).contains("\$") shouldBe false
    }

    private fun template(fileName: String): String {
        return javaClass.classLoader!!.getResourceAsStream(fileName).bufferedReader().use {
            it.readText()
        }
    }
}
