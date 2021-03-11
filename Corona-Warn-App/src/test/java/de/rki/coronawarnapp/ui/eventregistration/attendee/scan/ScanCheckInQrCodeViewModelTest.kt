package de.rki.coronawarnapp.ui.eventregistration.attendee.scan

import com.google.zxing.Result
import com.journeyapps.barcodescanner.BarcodeResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class ScanCheckInQrCodeViewModelTest : BaseTest() {

    private lateinit var viewModel: ScanCheckInQrCodeViewModel

    @BeforeEach
    fun setup() {
        viewModel = ScanCheckInQrCodeViewModel()
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
}
