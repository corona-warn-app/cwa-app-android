package de.rki.coronawarnapp.ui.submission.qrcode.scan

import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.service.submission.QRScanResult
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.util.formatter.TestResult
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
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

    @MockK lateinit var backgroundNoise: BackgroundNoise
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var riskLevelStorage: RiskLevelStorage

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(BackgroundNoise.Companion)
        every { BackgroundNoise.getInstance() } returns backgroundNoise
    }

    private fun createViewModel() = SubmissionQRCodeScanViewModel(
        submissionRepository,
        analyticsSettings,
        riskLevelStorage
    )

    @Test
    fun scanStatusValid() {
        val viewModel = createViewModel()

        // start
        viewModel.scanStatusValue.value = ScanStatus.STARTED

        viewModel.scanStatusValue.value shouldBe ScanStatus.STARTED

        QRCodeCensor.lastGUID = null

        // valid guid
        val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
        viewModel.validateTestGUID("https://localhost/?$guid")
        viewModel.scanStatusValue.let { Assert.assertEquals(ScanStatus.SUCCESS, it.value) }
        QRCodeCensor.lastGUID = guid

        // invalid guid
        viewModel.validateTestGUID("https://no-guid-here")
        viewModel.scanStatusValue.let { Assert.assertEquals(ScanStatus.INVALID, it.value) }
    }

    @Test
    fun `doDeviceRegistration checks if analytics is enabled`() {
        val scanResult = mockk<QRScanResult>().apply {
            every { guid } returns "123456-12345678-1234-4DA7-B166-B86D85475064"
        }
        every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(false)
        coEvery { submissionRepository.asyncRegisterDeviceViaGUID(any()) } returns TestResult.POSITIVE
        createViewModel().doDeviceRegistration(scanResult)

        verify {
            analyticsSettings.analyticsEnabled
            analyticsSettings.testScannedAfterConsent wasNot Called
        }
    }

    @Test
    fun `doDeviceRegistration sets test result settings if consent enabled`() {
        val scanResult = mockk<QRScanResult>().apply {
            every { guid } returns "123456-12345678-1234-4DA7-B166-B86D85475064"
        }
        every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(false)
        every { analyticsSettings.testResultAtRegistration } returns mockFlowPreference(null)
        coEvery { submissionRepository.asyncRegisterDeviceViaGUID(any()) } returns TestResult.POSITIVE

        createViewModel().doDeviceRegistration(scanResult)

        verify {
            analyticsSettings.testScannedAfterConsent
            analyticsSettings.testResultAtRegistration
        }
    }

    @Test
    fun `doDeviceRegistration saves time  POSTIVE test result received`() {
        val scanResult = mockk<QRScanResult>().apply {
            every { guid } returns "123456-12345678-1234-4DA7-B166-B86D85475064"
        }
        every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(false)
        every { analyticsSettings.testResultAtRegistration } returns mockFlowPreference(null)
        coEvery { submissionRepository.asyncRegisterDeviceViaGUID(any()) } returns TestResult.POSITIVE

        createViewModel().doDeviceRegistration(scanResult)

        verify {
            analyticsSettings.finalTestResultReceivedAt
        }
    }

    @Test
    fun `doDeviceRegistration saves time  NEGATIVE test result received`() {
        val scanResult = mockk<QRScanResult>().apply {
            every { guid } returns "123456-12345678-1234-4DA7-B166-B86D85475064"
        }
        every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(false)
        every { analyticsSettings.testResultAtRegistration } returns mockFlowPreference(null)
        coEvery { submissionRepository.asyncRegisterDeviceViaGUID(any()) } returns TestResult.NEGATIVE

        createViewModel().doDeviceRegistration(scanResult)

        verify {
            analyticsSettings.finalTestResultReceivedAt
        }
    }

    @Test
    fun `finalTestResultReceivedAt is not set when test result is PENDING`() {
        val scanResult = mockk<QRScanResult>().apply {
            every { guid } returns "123456-12345678-1234-4DA7-B166-B86D85475064"
        }
        every { analyticsSettings.analyticsEnabled } returns mockFlowPreference(true)
        every { analyticsSettings.testScannedAfterConsent } returns mockFlowPreference(false)
        every { analyticsSettings.testResultAtRegistration } returns mockFlowPreference(null)
        coEvery { submissionRepository.asyncRegisterDeviceViaGUID(any()) } returns TestResult.PENDING

        createViewModel().doDeviceRegistration(scanResult)

        verify {
            analyticsSettings.finalTestResultReceivedAt wasNot Called
        }
    }
}
