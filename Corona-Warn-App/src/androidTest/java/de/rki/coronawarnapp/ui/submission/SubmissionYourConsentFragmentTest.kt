package de.rki.coronawarnapp.ui.submission

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.yourconsent.SubmissionYourConsentFragment
import de.rki.coronawarnapp.ui.submission.yourconsent.SubmissionYourConsentFragmentArgs
import de.rki.coronawarnapp.ui.submission.yourconsent.SubmissionYourConsentViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.captureScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class SubmissionYourConsentFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository
    @MockK lateinit var testType: CoronaTest.Type

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private lateinit var viewModel: SubmissionYourConsentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { submissionRepository.testForType(any()) } returns flowOf()
        viewModel =
            SubmissionYourConsentViewModel(
                TestDispatcherProvider(),
                interoperabilityRepository,
                submissionRepository,
                testType
            )
        setupMockViewModel(
            object : SubmissionYourConsentViewModel.Factory {
                override fun create(testType: CoronaTest.Type): SubmissionYourConsentViewModel = viewModel
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
        captureScreenshot<SubmissionYourConsentFragment>(
            fragmentArgs = SubmissionYourConsentFragmentArgs(true, testType).toBundle()
        )
    }
}

@Module
abstract class SubmissionYourConsentFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun submissionYourConsentScreen(): SubmissionYourConsentFragment
}
