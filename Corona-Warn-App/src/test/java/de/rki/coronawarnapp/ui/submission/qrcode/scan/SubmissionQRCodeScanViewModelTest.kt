package de.rki.coronawarnapp.ui.submission.qrcode.scan

import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.util.permission.CameraSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
class SubmissionQRCodeScanViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var cameraSettings: CameraSettings
    @MockK lateinit var qrCodeValidator: CoronaTestQrCodeValidator

    private val coronaTestFlow = MutableStateFlow<CoronaTest?>(
        null
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { submissionRepository.testForType(any()) } returns coronaTestFlow
    }

    private fun createViewModel() = SubmissionQRCodeScanViewModel(
        TestDispatcherProvider(),
        submissionRepository,
        cameraSettings,
        isConsentGiven = true,
        qrCodeValidator,
    )

    @Test
    fun scanStatusValid() {
        // valid guid
        val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
        val coronaTestQRCode = CoronaTestQRCode.PCR(
            qrCodeGUID = guid
        )

        val validQrCode = "https://localhost/?$guid"
        val invalidQrCode = "https://no-guid-here"

        every { qrCodeValidator.validate(validQrCode) } returns coronaTestQRCode
        every { qrCodeValidator.validate(invalidQrCode) } throws InvalidQRCodeException()
        val viewModel = createViewModel()

        // start
        viewModel.scanStatusValue.value = ScanStatus.STARTED

        viewModel.scanStatusValue.value shouldBe ScanStatus.STARTED

        QRCodeCensor.lastGUID = null

        viewModel.validateTestGUID(validQrCode)
        viewModel.scanStatusValue.observeForever {}
        viewModel.scanStatusValue.value shouldBe ScanStatus.SUCCESS
        QRCodeCensor.lastGUID = guid

        // invalid guid
        viewModel.validateTestGUID(invalidQrCode)
        viewModel.scanStatusValue.value shouldBe ScanStatus.INVALID
    }

    @Test
    fun `doDeviceRegistration calls TestResultDataCollector`() = runBlockingTest {
        val viewModel = createViewModel()
        val mockResult = mockk<CoronaTestQRCode>().apply {
            every { registrationIdentifier } returns "guid"
        }
        val mockTest = mockk<CoronaTest>()
        coEvery { submissionRepository.registerTest(any()) } returns mockTest
        viewModel.doDeviceRegistration(mockResult)
    }

    @Test
    fun `Camera settings is saved when user denies it`() {
        every { cameraSettings.isCameraDeniedPermanently } returns mockFlowPreference(false)
        createViewModel().setCameraDeniedPermanently(true)

        verify { cameraSettings.isCameraDeniedPermanently }
    }
}
