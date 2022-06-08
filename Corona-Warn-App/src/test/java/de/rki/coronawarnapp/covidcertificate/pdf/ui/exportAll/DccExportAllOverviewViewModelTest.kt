package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.helper.CertificateTemplate
import de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.helper.HTML_TEMPLATE
import de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.helper.qrCodeBase64
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.files.FileSharing
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import java.io.File
import java.time.Instant

@ExtendWith(InstantExecutorExtension::class)
internal class DccExportAllOverviewViewModelTest : BaseTest() {

    @MockK lateinit var personCertificatesProvider: CertificateProvider
    @MockK lateinit var template: CertificateTemplate
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var fileSharing: FileSharing

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.helper.CertificateDataInjectorKt")

        val vc = mockk<VaccinationCertificate>().apply {
            every { fullNameFormatted } returns "Full Name"
            every { dateOfBirthFormatted } returns "1990-10-10"
            every { uniqueCertificateIdentifier } returns "UNIQUE_CERTIFICATE_IDENTIFIER"
            every { targetDisease } returns "Covid 19"
            every { certificateCountry } returns "Germany"
            every { qrCodeBase64() } returns "1234"
            every { certificateIssuer } returns "Robert Koch"
            every { medicalProductName } returns "mRNa"
            every { vaccineManufacturer } returns "BionTech"
            every { doseNumber } returns 1
            every { totalSeriesOfDoses } returns 2
            every { vaccinatedOnFormatted } returns "2020-10-12"
            every { vaccineTypeName } returns "Astra"
            every { state } returns CwaCovidCertificate.State.Valid(org.joda.time.Instant.now())
            every { vaccinatedOn } returns LocalDate.now()
            every { fullName } returns "A"
        }

        val rc = mockk<RecoveryCertificate>().apply {
            every { fullNameFormatted } returns "Full Name"
            every { dateOfBirthFormatted } returns "1990-10-10"
            every { uniqueCertificateIdentifier } returns "UNIQUE_CERTIFICATE_IDENTIFIER"
            every { targetDisease } returns "Covid 19"
            every { certificateCountry } returns "Germany"
            every { qrCodeBase64() } returns "1234"
            every { certificateIssuer } returns "Robert Koch"
            every { validFromFormatted } returns "2020-12-10"
            every { validUntilFormatted } returns "2021-12-10"
            every { testedPositiveOnFormatted } returns "2021-11-10"
            every { state } returns CwaCovidCertificate.State.Valid(org.joda.time.Instant.now())
            every { validFrom } returns LocalDate.now()
            every { validUntil } returns LocalDate.now()
            every { fullName } returns "A"
            every { testedPositiveOn } returns LocalDate.now()
        }

        val tc = mockk<TestCertificate>().apply {
            every { fullNameFormatted } returns "Full Name"
            every { dateOfBirthFormatted } returns "1990-10-10"
            every { uniqueCertificateIdentifier } returns "UNIQUE_CERTIFICATE_IDENTIFIER"
            every { targetDisease } returns "Covid 19"
            every { certificateCountry } returns "Germany"
            every { qrCodeBase64() } returns "1234"
            every { certificateIssuer } returns "Robert Koch"
            every { testName } returns "Rapid"
            every { testNameAndManufacturer } returns "Robert Koch"
            every { sampleCollectedAtFormatted } returns "2020-10-20"
            every { testResult } returns "Negative"
            every { testCenter } returns "TestIQ"
            every { testType } returns "Rapid"
            every { state } returns CwaCovidCertificate.State.Valid(org.joda.time.Instant.now())
            every { sampleCollectedAt } returns org.joda.time.Instant.now()
            every { fullName } returns "A"
        }
        every { template.invoke(any()) } returns "Template"
        every { personCertificatesProvider.certificateContainer } returns flowOf(
            mockk<CertificateProvider.CertificateContainer>().apply {
                every { allCwaCertificates } returns setOf(vc, rc, tc)
            }
        )
        every { timeStamper.nowJavaUTC } returns Instant.now()
    }

    @Test
    fun getPdfString() {
        instance().pdfString.getOrAwaitValue() shouldBe HTML_TEMPLATE.replace(
            oldValue = "++certificates++",
            newValue = """
                <li>Template</li>
                <li>Template</li>
                <li>Template</li>
            """.trimIndent()
        )
    }

    fun instance() = DccExportAllOverviewViewModel(
        personCertificatesProvider = personCertificatesProvider,
        template = template,
        timeStamper = timeStamper,
        dispatcher = TestDispatcherProvider(),
        fileSharing = fileSharing,
        path = File("")
    )
}
