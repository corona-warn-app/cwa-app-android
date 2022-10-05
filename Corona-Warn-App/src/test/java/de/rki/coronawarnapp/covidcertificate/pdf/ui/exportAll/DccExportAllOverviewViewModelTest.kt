package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import android.print.FilePrinter
import android.print.PrintManager
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateTemplate
import de.rki.coronawarnapp.covidcertificate.pdf.core.appendPage
import de.rki.coronawarnapp.covidcertificate.pdf.core.buildHtml
import de.rki.coronawarnapp.covidcertificate.pdf.core.qrCodeBase64
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.files.FileSharing
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import java.io.File
import java.time.Instant
import java.time.LocalDate

@ExtendWith(InstantExecutorExtension::class)
internal class DccExportAllOverviewViewModelTest : BaseTest() {

    @MockK lateinit var personCertificatesProvider: CertificateProvider
    @MockK lateinit var template: CertificateTemplate
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var fileSharing: FileSharing
    @MockK lateinit var filePrinter: FilePrinter
    @MockK lateinit var printManager: PrintManager

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("de.rki.coronawarnapp.covidcertificate.pdf.core.CertificateDataInjectorKt")

        every { printManager.print(any(), any(), any()) } returns mockk()
        coEvery { filePrinter.print(any(), any(), any()) } just Runs
        every { fileSharing.getFileIntentProvider(any(), any(), any()) } returns mockk()

        val vc = mockk<VaccinationCertificate>().apply {
            every { fullNameFormatted } returns "Full Name"
            every { rawCertificate.dob } returns "1990-10-10"
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
            every { state } returns CwaCovidCertificate.State.Valid(Instant.now())
            every { rawCertificate.payload.certificateCountry } returns "DE"
            every { vaccinatedOn } returns LocalDate.now()
            every { fullName } returns "A"
            every { rawCertificate.vaccination.totalSeriesOfDoses } returns 2
            every { rawCertificate.vaccination.doseNumber } returns 1
            every { rawCertificate.vaccination.dt } returns "2020-10-12"
            every { rawCertificate.payload.certificateIssuer } returns "Robert Koch"
            every { rawCertificate.payload.certificateCountry } returns "Germany"
        }

        val rc = mockk<RecoveryCertificate>().apply {
            every { fullNameFormatted } returns "Full Name"
            every { uniqueCertificateIdentifier } returns "UNIQUE_CERTIFICATE_IDENTIFIER"
            every { targetDisease } returns "Covid 19"
            every { certificateCountry } returns "Germany"
            every { qrCodeBase64() } returns "1234"
            every { certificateIssuer } returns "Robert Koch"
            every { validFromFormatted } returns "2020-12-10"
            every { validUntilFormatted } returns "2021-12-10"
            every { testedPositiveOnFormatted } returns "2021-11-10"
            every { state } returns CwaCovidCertificate.State.Valid(Instant.now())
            every { validFrom } returns LocalDate.now()
            every { validUntil } returns LocalDate.now()
            every { fullName } returns "A"
            every { testedPositiveOn } returns LocalDate.now()
            every { rawCertificate.dob } returns "1990-10-10"
            every { rawCertificate.recovery.df } returns "2020-12-10"
            every { rawCertificate.recovery.du } returns "2021-12-10"
            every { rawCertificate.recovery.fr } returns "2021-11-10"
            every { rawCertificate.payload.certificateIssuer } returns "Robert Koch"
            every { rawCertificate.payload.certificateCountry } returns "Germany"
        }

        val tc = mockk<TestCertificate>().apply {
            every { fullNameFormatted } returns "Full Name"
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
            every { state } returns CwaCovidCertificate.State.Valid(Instant.now())
            every { sampleCollectedAt } returns Instant.now()
            every { fullName } returns "A"
            every { rawCertificate.dob } returns "1990-10-10"
            every { rawCertificate.payload.certificateIssuer } returns "Robert Koch"
            every { rawCertificate.payload.certificateCountry } returns "Germany"
            every { rawCertificate.test.sc } returns "2020-10-20"
            every { rawCertificate.test.testCenter } returns "TestIQ"
        }
        every { template.invoke(any()) } returns "Template"
        every { personCertificatesProvider.certificateContainer } returns flowOf(
            mockk<CertificateProvider.CertificateContainer>().apply {
                every { allCwaCertificates } returns setOf(vc, rc, tc)
            }
        )
        every { timeStamper.nowUTC } returns Instant.now()
    }

    @Test
    fun `Pdf String`() {
        instance().pdfString.getOrAwaitValue() shouldBe buildHtml {
            appendPage("Template")
            appendPage("Template")
            appendPage("Template")
        }
    }

    @Test
    fun `No certificates to export`() {
        every { personCertificatesProvider.certificateContainer } returns flowOf(
            mockk<CertificateProvider.CertificateContainer>().apply {
                every { allCwaCertificates } returns setOf()
            }
        )
        instance().apply {
            pdfString.getOrAwaitValue() shouldBe buildHtml {}
            exportResult.getOrAwaitValue() shouldBe DccExportAllOverviewViewModel.EmptyResult
        }
    }

    @Test
    fun `Print document`() {
        instance().apply {
            print(mockk())
            val result = exportResult.getOrAwaitValue()
            result.shouldBeInstanceOf<DccExportAllOverviewViewModel.PrintResult>()
        }
    }

    @Test
    fun `Create PDF`() {
        instance().apply {
            createPDF(mockk())
            coVerify { filePrinter.print(any(), any(), any()) }
            exportResult.getOrAwaitValue().shouldBeInstanceOf<DccExportAllOverviewViewModel.PDFResult>()
        }
    }

    @Test
    fun `Share PDF`() {
        instance().apply {
            sharePDF()
            exportResult.getOrAwaitValue().shouldBeInstanceOf<DccExportAllOverviewViewModel.ShareResult>()
            verify {
                fileSharing.getFileIntentProvider(any(), any(), any())
            }
        }
    }

    fun instance() = DccExportAllOverviewViewModel(
        personCertificatesProvider = personCertificatesProvider,
        template = template,
        timeStamper = timeStamper,
        dispatcher = TestDispatcherProvider(),
        fileSharing = fileSharing,
        path = File(""),
        filePrinter = filePrinter
    )
}
