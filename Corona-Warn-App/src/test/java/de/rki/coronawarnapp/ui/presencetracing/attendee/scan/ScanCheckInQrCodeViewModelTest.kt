package de.rki.coronawarnapp.ui.presencetracing.attendee.scan

import com.google.zxing.Result
import com.journeyapps.barcodescanner.BarcodeResult
import de.rki.coronawarnapp.util.permission.CameraSettings
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import testhelpers.preferences.mockFlowPreference

@ExtendWith(InstantExecutorExtension::class)
class ScanCheckInQrCodeViewModelTest : BaseTest() {

    private lateinit var viewModel: ScanCheckInQrCodeViewModel
    @MockK lateinit var cameraSettings: CameraSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = ScanCheckInQrCodeViewModel(
            cameraSettings
        )
    }

    @Test
    fun `onNavigateUp goes back`() {
        viewModel.onNavigateUp()
        viewModel.events.getOrAwaitValue() shouldBe ScanCheckInQrCodeNavigation.BackNavigation
    }

    @Test
    fun `onScanResult results in navigation url`() {
        val mockedResult = mockk<BarcodeResult>().apply {
            every { result } returns mockk<Result>().apply {
                every { text } returns "https://coronawarn.app/E1/SOME_PATH_GOES_HERE"
            }
        }
        viewModel.onScanResult(mockedResult)
        viewModel.events.getOrAwaitValue() shouldBe
            ScanCheckInQrCodeNavigation.ScanResultNavigation("https://coronawarn.app/E1/SOME_PATH_GOES_HERE")
    }

    @Test
    fun `Camera settings is saved when user denies it`() {
        every { cameraSettings.isCameraDeniedPermanently } returns mockFlowPreference(false)
        viewModel.setCameraDeniedPermanently(true)

        verify { cameraSettings.isCameraDeniedPermanently }
    }
}
