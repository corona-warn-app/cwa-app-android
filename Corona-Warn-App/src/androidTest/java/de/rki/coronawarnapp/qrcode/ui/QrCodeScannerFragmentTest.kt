package de.rki.coronawarnapp.qrcode.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class QrCodeScannerFragmentTest : BaseUITest() {

    @MockK lateinit var qrcodeScannerViewModel: QrCodeScannerViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Screenshot
    @Test
    fun qrcodeScannerScreenshot() {
        launchFragmentInContainer2<QrCodeScannerFragment>()
        takeScreenshot<QrCodeScannerFragment>()
    }

    @Screenshot
    @Test
    fun restoreDgcDialog() {
        every { qrcodeScannerViewModel.result } returns SingleLiveEvent<ScannerResult>()
            .apply { postValue(DccResult.InRecycleBin(mockk())) }
        launchFragmentInContainer2<QrCodeScannerFragment>()
        takeScreenshot<QrCodeScannerFragment>()
    }

    @Screenshot
    @Test
    fun restoreCoronaTestDialog() {
        every { qrcodeScannerViewModel.result } returns SingleLiveEvent<ScannerResult>()
            .apply { postValue(CoronaTestResult.InRecycleBin(mockk())) }
        launchFragmentInContainer2<QrCodeScannerFragment>()
        takeScreenshot<QrCodeScannerFragment>()
    }

    @Test
    fun launch() {
        launchFragmentInContainer2<QrCodeScannerFragment>()
    }
}
