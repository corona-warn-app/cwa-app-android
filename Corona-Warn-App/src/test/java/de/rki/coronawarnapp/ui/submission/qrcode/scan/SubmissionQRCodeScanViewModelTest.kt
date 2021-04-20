package de.rki.coronawarnapp.ui.submission.qrcode.scan

import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeSubmission
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeSubmission.ValidationState
import de.rki.coronawarnapp.util.permission.CameraSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
class SubmissionQRCodeScanViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var cameraSettings: CameraSettings
    @MockK lateinit var qrCodeValidator: CoronaTestQrCodeValidator
    @MockK lateinit var qrCodeSubmission: QrCodeSubmission

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createViewModel() = SubmissionQRCodeScanViewModel(
        cameraSettings,
        qrCodeSubmission
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
        viewModel.qrCodeValidationState.value = ValidationState.STARTED

        viewModel.qrCodeValidationState.value shouldBe ValidationState.STARTED

        QRCodeCensor.lastGUID = null

        viewModel.onQrCodeAvailable(validQrCode)
        viewModel.qrCodeValidationState.let { Assert.assertEquals(ValidationState.SUCCESS, it.value) }
        QRCodeCensor.lastGUID = guid

        // invalid guid
        viewModel.onQrCodeAvailable(invalidQrCode)
        viewModel.qrCodeValidationState.let { Assert.assertEquals(ValidationState.INVALID, it.value) }
    }

    @Test
    fun `doDeviceRegistration calls TestResultDataCollector`() {
        val viewModel = createViewModel()
        val mockResult = mockk<CoronaTestQRCode>().apply {
            every { registrationIdentifier } returns "guid"
        }
        val mockTest = mockk<CoronaTest>()
        coEvery { submissionRepository.registerTest(any()) } returns mockTest
        //viewModel.doDeviceRegistration(mockResult)
    }

    @Test
    fun `Camera settings is saved when user denies it`() {
        every { cameraSettings.isCameraDeniedPermanently } returns mockFlowPreference(false)
        createViewModel().setCameraDeniedPermanently(true)

        verify { cameraSettings.isCameraDeniedPermanently }
    }
}
