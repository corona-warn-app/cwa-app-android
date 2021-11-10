package de.rki.coronawarnapp.qrcode.ui

import android.net.Uri
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.qrcode.QrCodeFileParser
import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import de.rki.coronawarnapp.qrcode.scanner.ImportDocumentException
import de.rki.coronawarnapp.qrcode.scanner.QrCodeValidator
import de.rki.coronawarnapp.qrcode.scanner.UnsupportedQrCodeException
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.TestPending
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.TestNegative
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.permission.CameraSettings
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.RestoreDuplicateTest
import de.rki.coronawarnapp.reyclebin.coronatest.request.toRestoreRecycledTestRequest
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import testhelpers.preferences.mockFlowPreference
import javax.inject.Inject

@ExtendWith(InstantExecutorExtension::class)
class QrCodeScannerViewModelTest : BaseTest() {
    @Inject lateinit var qrCodeValidator: QrCodeValidator
    @MockK lateinit var cameraSettings: CameraSettings
    @MockK lateinit var qrCodeFileParser: QrCodeFileParser
    @MockK lateinit var dccHandler: DccQrCodeHandler
    @MockK lateinit var checkInHandler: CheckInQrCodeHandler
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var dccSettings: CovidCertificateSettings
    @MockK lateinit var traceLocationSettings: TraceLocationSettings
    @MockK lateinit var recycledCertificatesProvider: RecycledCertificatesProvider
    @MockK lateinit var recycledCoronaTestsProvider: RecycledCoronaTestsProvider

    private val recycledRAT = RACoronaTest(
        identifier = "rat-identifier",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token",
        testResult = CoronaTestResult.RAT_REDEEMED,
        testedAt = Instant.EPOCH,
        isDccConsentGiven = false,
        isDccSupportedByPoc = false,
    )

    private val anotherRAT = RACoronaTest(
        identifier = "rat-identifier-another",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token-another",
        testResult = CoronaTestResult.RAT_REDEEMED,
        testedAt = Instant.EPOCH,
        isDccConsentGiven = false,
        isDccSupportedByPoc = false
    )

    private val recycledPCR = PCRCoronaTest(
        identifier = "pcr-identifier",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token",
        testResult = CoronaTestResult.PCR_NEGATIVE,
        isDccConsentGiven = true
    )

    private val anotherPCR = PCRCoronaTest(
        identifier = "pcr-identifier-another",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token-another",
        testResult = CoronaTestResult.PCR_NEGATIVE,
        isDccConsentGiven = true
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(Uri::class)
        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        every { cameraSettings.isCameraDeniedPermanently } returns mockFlowPreference(false)
        every { Uri.parse(any()) } returns mockk()
        coEvery { qrCodeFileParser.decodeQrCodeFile(any()) } returns QrCodeFileParser.ParseResult.Success("qrcode")
        every { recycledCoronaTestsProvider.tests } returns flowOf(emptySet())
        coEvery { recycledCoronaTestsProvider.restoreCoronaTest(any()) } just Runs
    }

    @Test
    fun `onImportFile triggers file parsing`() {
        viewModel().onImportFile(mockk())
        coVerify { qrCodeFileParser.decodeQrCodeFile(any()) }
    }

    @Test
    fun `onImportFile report error in case of failure`() {
        coEvery { qrCodeFileParser.decodeQrCodeFile(any()) } returns QrCodeFileParser.ParseResult.Failure(Exception())

        viewModel().apply {
            onImportFile(mockk())
            result.value.shouldBeInstanceOf<Error>()
        }
    }

    @Test
    fun `onImportFile report error in case of crash`() {
        coEvery { qrCodeFileParser.decodeQrCodeFile(any()) } throws Exception()

        viewModel().apply {
            onImportFile(mockk())
            val result = result.value
            result.shouldBeInstanceOf<Error>()
            result.error.shouldBeInstanceOf<ImportDocumentException>().errorCode shouldBe
                ImportDocumentException.ErrorCode.CANT_READ_FILE
        }
    }

    @Test
    fun `onScanResult reports UnsupportedQrCodeException`() {
        viewModel().apply {
            onScanResult("rawResult")
            result.value.shouldBeInstanceOf<Error>().error
                .shouldBeInstanceOf<UnsupportedQrCodeException>().errorCode shouldBe
                UnsupportedQrCodeException.ErrorCode.UNSUPPORTED_QR_CODE
        }
    }

    @Test
    fun setCameraDeniedPermanently() {
        viewModel().setCameraDeniedPermanently(true)
        verify { cameraSettings.isCameraDeniedPermanently }
    }

    @Test
    fun `restoreCertificate asks provider to restore DGC`() {
        coEvery { recycledCertificatesProvider.restoreCertificate(any()) } just Runs
        val containerId = TestCertificateContainerId("ceruuid")
        viewModel().apply {
            restoreCertificate(containerId)
            result.getOrAwaitValue().shouldBeInstanceOf<DccResult.Details>()
        }
        coVerify { recycledCertificatesProvider.restoreCertificate(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test when another PCR is active`() {
        every { submissionRepository.testForType(CoronaTest.Type.PCR) } returns flowOf(anotherPCR)
        viewModel().apply {
            restoreCoronaTest(recycledPCR)
            result.getOrAwaitValue() shouldBe RestoreDuplicateTest(recycledPCR.toRestoreRecycledTestRequest())
        }
        coVerify(exactly = 0) { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test when another RAT is active`() {
        every { submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN) } returns flowOf(anotherRAT)

        viewModel().apply {
            restoreCoronaTest(recycledRAT)
            result.getOrAwaitValue() shouldBe RestoreDuplicateTest(recycledRAT.toRestoreRecycledTestRequest())
        }
        coVerify(exactly = 0) { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is pending`() {
        every { submissionRepository.testForType(CoronaTest.Type.PCR) } returns flowOf(null)
        val recycledCoronaTest = recycledPCR.copy(testResult = CoronaTestResult.PCR_OR_RAT_PENDING)
        viewModel().apply {
            restoreCoronaTest(recycledCoronaTest)
            result.getOrAwaitValue() shouldBe TestPending(
                recycledCoronaTest
            )
        }
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test is pending`() {
        every { submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN) } returns flowOf(null)

        val recycledCoronaTest = recycledRAT.copy(testResult = CoronaTestResult.PCR_OR_RAT_PENDING)
        viewModel().apply {
            restoreCoronaTest(recycledCoronaTest)
            result.getOrAwaitValue() shouldBe TestPending(recycledCoronaTest)
        }
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is negative`() {
        every { submissionRepository.testForType(CoronaTest.Type.PCR) } returns flowOf(null)
        val recycledCoronaTest = recycledPCR.copy(testResult = CoronaTestResult.PCR_NEGATIVE)
        viewModel().apply {
            restoreCoronaTest(recycledCoronaTest)
            result.getOrAwaitValue() shouldBe TestNegative(
                recycledCoronaTest
            )
        }
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test is negative`() {
        every { submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN) } returns flowOf(null)

        val recycledCoronaTest = recycledRAT.copy(testResult = CoronaTestResult.RAT_NEGATIVE)
        viewModel().apply {
            restoreCoronaTest(recycledCoronaTest)
            result.getOrAwaitValue() shouldBe TestNegative(recycledCoronaTest)
        }
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is not pending`() {
        every { submissionRepository.testForType(CoronaTest.Type.PCR) } returns flowOf(null)
        viewModel().apply {
            restoreCoronaTest(recycledPCR)
        }
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test is not pending`() {
        every { submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN) } returns flowOf(null)

        viewModel().apply {
            restoreCoronaTest(recycledRAT)
        }
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    fun viewModel() = QrCodeScannerViewModel(
        qrCodeFileParser = qrCodeFileParser,
        dccHandler = dccHandler,
        submissionRepository = submissionRepository,
        checkInHandler = checkInHandler,
        dccSettings = dccSettings,
        traceLocationSettings = traceLocationSettings,
        dispatcherProvider = TestDispatcherProvider(),
        cameraSettings = cameraSettings,
        qrCodeValidator = qrCodeValidator,
        recycledCertificatesProvider = recycledCertificatesProvider,
        recycledCoronaTestsProvider = recycledCoronaTestsProvider
    )
}
