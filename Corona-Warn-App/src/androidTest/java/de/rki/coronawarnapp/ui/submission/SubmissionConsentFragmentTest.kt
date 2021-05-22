package de.rki.coronawarnapp.ui.submission

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.nearby.modules.tekhistory.TEKHistoryProvider
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.qrcode.QrCodeRegistrationStateProcessor
import de.rki.coronawarnapp.ui.submission.qrcode.consent.SubmissionConsentFragment
import de.rki.coronawarnapp.ui.submission.qrcode.consent.SubmissionConsentFragmentArgs
import de.rki.coronawarnapp.ui.submission.qrcode.consent.SubmissionConsentViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.captureScreenshot

@RunWith(AndroidJUnit4::class)
class SubmissionConsentFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository
    @MockK lateinit var tekHistoryProvider: TEKHistoryProvider
    @MockK lateinit var qrCodeRegistrationStateProcessor: QrCodeRegistrationStateProcessor
    @MockK lateinit var qrCodeValidator: CoronaTestQrCodeValidator

    private lateinit var viewModel: SubmissionConsentViewModel

    private val fragmentArgs = SubmissionConsentFragmentArgs(
        qrCode = null
    ).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { interoperabilityRepository.countryList } returns flowOf()
        viewModel = SubmissionConsentViewModel(
            interoperabilityRepository,
            TestDispatcherProvider(),
            tekHistoryProvider,
            qrCodeRegistrationStateProcessor,
            submissionRepository,
            qrCodeValidator
        )
        setupMockViewModel(
            object : SubmissionConsentViewModel.Factory {
                override fun create(): SubmissionConsentViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    @Screenshot
    fun capture_fragment_results() {
        captureScreenshot<SubmissionConsentFragment>(
            fragmentArgs = fragmentArgs
        )
    }
}

@Module
abstract class SubmissionConsentFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun submissionConsentScreen(): SubmissionConsentFragment
}
