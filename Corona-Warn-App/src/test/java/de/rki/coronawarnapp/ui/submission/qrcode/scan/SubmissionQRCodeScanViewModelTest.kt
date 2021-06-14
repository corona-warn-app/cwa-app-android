package de.rki.coronawarnapp.ui.submission.qrcode.scan

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor.ValidationState
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
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
    @MockK lateinit var qrCodeRegistrationStateProcessor: QrCodeRegistrationStateProcessor
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { submissionRepository.testForType(any()) } returns MutableStateFlow<CoronaTest?>(null)
        coEvery { qrCodeRegistrationStateProcessor.showRedeemedTokenWarning } returns SingleLiveEvent()
        coEvery { qrCodeRegistrationStateProcessor.registrationState } returns MutableLiveData(
            QrCodeRegistrationStateProcessor.RegistrationState(ApiRequestState.IDLE)
        )
        coEvery { qrCodeRegistrationStateProcessor.registrationError } returns SingleLiveEvent()
    }

    private fun createViewModel() = SubmissionQRCodeScanViewModel(
        isConsentGiven = true,
        dispatcherProvider = TestDispatcherProvider(),
        cameraSettings = cameraSettings,
        qrCodeRegistrationStateProcessor = qrCodeRegistrationStateProcessor,
        submissionRepository = submissionRepository,
        qrCodeValidator = qrCodeValidator,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector
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

        viewModel.registerCoronaTest(validQrCode)
        viewModel.qrCodeValidationState.observeForever {}
        viewModel.qrCodeValidationState.value shouldBe ValidationState.SUCCESS

        // invalid guid
        viewModel.registerCoronaTest(invalidQrCode)
        viewModel.qrCodeValidationState.value shouldBe ValidationState.INVALID
    }

    @Test
    fun `Camera settings is saved when user denies it`() {
        every { cameraSettings.isCameraDeniedPermanently } returns mockFlowPreference(false)
        createViewModel().setCameraDeniedPermanently(true)

        verify { cameraSettings.isCameraDeniedPermanently }
    }

    @Test
    fun `registerCoronaTest() should call analyticsKeySubmissionCollector for PCR tests`() =
        runBlockingTest {
            val coronaTestQRCode = CoronaTestQRCode.PCR(qrCodeGUID = "123456-12345678-1234-4DA7-B166-B86D85475064")

            every { qrCodeValidator.validate(any()) } returns coronaTestQRCode
            every { analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any()) } just Runs
            coEvery { qrCodeRegistrationStateProcessor.startQrCodeRegistration(any(), any()) } just Runs

            createViewModel().registerCoronaTest(rawResult = "")

            verify(exactly = 0) {
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.PCR)
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.RAPID_ANTIGEN)
            }
        }

    @Test
    fun `registerCoronaTest() should NOT call analyticsKeySubmissionCollector for RAT tests`() =
        runBlockingTest {
            val coronaTestQRCode = CoronaTestQRCode.PCR(qrCodeGUID = "123456-12345678-1234-4DA7-B166-B86D85475064")

            every { qrCodeValidator.validate(any()) } returns coronaTestQRCode
            every { analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any()) } just Runs
            coEvery { qrCodeRegistrationStateProcessor.startQrCodeRegistration(any(), any()) } just Runs

            createViewModel().registerCoronaTest(rawResult = "")

            verify(exactly = 0) {
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.PCR)
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.RAPID_ANTIGEN)
            }
        }

    @Test
    fun `registerCoronaTest() should call analyticsKeySubmissionCollector for RAT tests - no-dcc support`() =
        runBlockingTest {
            val coronaTestQRCode = CoronaTestQRCode.RapidAntigen(
                hash = "123456-12345678-1234-4DA7-B166-B86D85475064",
                createdAt = Instant.EPOCH,
                isDccSupportedByPoc = false
            )

            every { qrCodeValidator.validate(any()) } returns coronaTestQRCode
            every { analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any()) } just Runs
            coEvery { qrCodeRegistrationStateProcessor.startQrCodeRegistration(any(), any()) } just Runs

            createViewModel().registerCoronaTest(rawResult = "")

            verify(exactly = 1) {
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.RAPID_ANTIGEN)
            }
            verify(exactly = 0) {
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.PCR)
            }
        }

    @Test
    fun `registerCoronaTest() should Not call analyticsKeySubmissionCollector for RAT tests - dcc support`() =
        runBlockingTest {
            val coronaTestQRCode = CoronaTestQRCode.RapidAntigen(
                hash = "123456-12345678-1234-4DA7-B166-B86D85475064",
                createdAt = Instant.EPOCH,
                isDccSupportedByPoc = true
            )

            every { qrCodeValidator.validate(any()) } returns coronaTestQRCode
            every { analyticsKeySubmissionCollector.reportAdvancedConsentGiven(any()) } just Runs
            coEvery { qrCodeRegistrationStateProcessor.startQrCodeRegistration(any(), any()) } just Runs

            createViewModel().registerCoronaTest(rawResult = "")
            verify(exactly = 0) {
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.RAPID_ANTIGEN)
                analyticsKeySubmissionCollector.reportAdvancedConsentGiven(CoronaTest.Type.PCR)
            }
        }
}
