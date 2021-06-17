package de.rki.coronawarnapp.ui.submission.qrcode.scan

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.util.permission.CameraSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
    @MockK lateinit var testRegistrationStateProcessor: TestRegistrationStateProcessor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { submissionRepository.testForType(any()) } returns MutableStateFlow<CoronaTest?>(null)

        testRegistrationStateProcessor.apply {
            every { state } returns flowOf(TestRegistrationStateProcessor.State.Idle)
            coEvery { startRegistration(any(), any(), any()) } returns mockk()
        }
    }

    private fun createViewModel() = SubmissionQRCodeScanViewModel(
        isConsentGiven = true,
        dispatcherProvider = TestDispatcherProvider(),
        cameraSettings = cameraSettings,
        registrationStateProcessor = testRegistrationStateProcessor,
        submissionRepository = submissionRepository,
        qrCodeValidator = qrCodeValidator,
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

        val expectedError = InvalidQRCodeException()
        every { qrCodeValidator.validate(invalidQrCode) } throws expectedError

        val viewModel = createViewModel()

        viewModel.qrCodeErrorEvent.observeForever {}
        // start
        viewModel.qrCodeErrorEvent.value shouldBe null

        viewModel.registerCoronaTest(validQrCode)
        viewModel.qrCodeErrorEvent.value shouldBe null

        // invalid guid
        viewModel.registerCoronaTest(invalidQrCode)
        viewModel.qrCodeErrorEvent.value shouldBe expectedError
    }

    @Test
    fun `Camera settings is saved when user denies it`() {
        every { cameraSettings.isCameraDeniedPermanently } returns mockFlowPreference(false)
        createViewModel().setCameraDeniedPermanently(true)

        verify { cameraSettings.isCameraDeniedPermanently }
    }
}
