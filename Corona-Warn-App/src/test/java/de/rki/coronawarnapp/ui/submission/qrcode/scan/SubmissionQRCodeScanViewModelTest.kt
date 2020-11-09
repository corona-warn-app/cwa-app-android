package de.rki.coronawarnapp.ui.submission.qrcode.scan

import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.submission.ScanStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
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

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)
        every { LocalData.testGUID(any()) } just Runs

        mockkObject(BackgroundNoise.Companion)
        every { BackgroundNoise.getInstance() } returns backgroundNoise
    }

    private fun createViewModel() = SubmissionQRCodeScanViewModel()

    @Test
    fun scanStatusValid() {
        val viewModel = createViewModel()

        // start
        viewModel.scanStatusValue.value = ScanStatus.STARTED

        viewModel.scanStatusValue.value shouldBe ScanStatus.STARTED

        // valid guid
        val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
        viewModel.validateTestGUID("https://localhost/?$guid")
        viewModel.scanStatusValue.let { Assert.assertEquals(ScanStatus.SUCCESS, it.value) }

        // invalid guid
        viewModel.validateTestGUID("https://no-guid-here")
        viewModel.scanStatusValue.let { Assert.assertEquals(ScanStatus.INVALID, it.value) }
    }
}
