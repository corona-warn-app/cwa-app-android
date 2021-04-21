package de.rki.coronawarnapp.ui.submission

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.submission.qrcode.scan.SubmissionQRCodeScanFragment
import de.rki.coronawarnapp.ui.submission.qrcode.scan.SubmissionQRCodeScanFragmentArgs
import de.rki.coronawarnapp.ui.submission.qrcode.scan.SubmissionQRCodeScanViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.launchFragment2

@RunWith(AndroidJUnit4::class)
class SubmissionQrCodeScanFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionQRCodeScanViewModel

    private var fragmentArgs = SubmissionQRCodeScanFragmentArgs(isConsentGiven = true).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : SubmissionQRCodeScanViewModel.Factory {
                override fun create(isConsentGiven: Boolean): SubmissionQRCodeScanViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<SubmissionQRCodeScanFragment>(
            fragmentArgs = fragmentArgs
        )
    }
}

@Module
abstract class SubmissionQRScanFragmentModule {
    @ContributesAndroidInjector
    abstract fun submissionQRScanScreen(): SubmissionQRCodeScanFragment
}
