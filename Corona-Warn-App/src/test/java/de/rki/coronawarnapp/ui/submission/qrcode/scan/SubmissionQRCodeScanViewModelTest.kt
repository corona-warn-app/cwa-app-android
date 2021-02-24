package de.rki.coronawarnapp.ui.submission.qrcode.scan

import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.datadonation.analytics.modules.registeredtest.TestResultDataCollector
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.service.submission.QRScanResult
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.util.formatter.TestResult
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionQRCodeScanViewModelTest : BaseTest() {

    @MockK lateinit var backgroundNoise: BackgroundNoise
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var testResultDataCollector: TestResultDataCollector

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(BackgroundNoise.Companion)
        every { BackgroundNoise.getInstance() } returns backgroundNoise
    }

    private fun createViewModel() = SubmissionQRCodeScanViewModel(
        submissionRepository,
        testResultDataCollector
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
    fun `doDeviceRegistration calls TestResultDataCollector`() {
        val viewModel = createViewModel()
        val mockResult = mockk<QRScanResult>().apply {
            every { guid } returns "guid"
        }

        coEvery { submissionRepository.asyncRegisterDeviceViaGUID(any()) } returns TestResult.POSITIVE
        viewModel.doDeviceRegistration(mockResult)

        coVerify { testResultDataCollector.saveTestResultAnalyticsSettings(any()) }
    }
}
