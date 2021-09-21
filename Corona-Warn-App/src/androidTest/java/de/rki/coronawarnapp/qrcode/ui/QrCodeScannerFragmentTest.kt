package de.rki.coronawarnapp.qrcode.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class QrCodeScannerFragmentTest : BaseUITest() {

    @MockK lateinit var qrcodeScannerViewModel: QrCodeScannerViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        with(qrcodeScannerViewModel) {
        }

        setupMockViewModel(
            object : QrCodeScannerViewModel.Factory {
                override fun create(): QrCodeScannerViewModel = qrcodeScannerViewModel
            }
        )
    }

    @Screenshot
    @Test
    fun qrcodeScannerScreenshot() {
        launchFragmentInContainer2<QrCodeScannerFragment>()
        takeScreenshot<QrCodeScannerFragment>()
    }

    @Test
    fun launch() {
        launchFragment2<QrCodeScannerFragment>()
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }
}

@Module
abstract class QrCodeScannerFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun qrcodeScannerFragment(): QrCodeScannerFragment
}
