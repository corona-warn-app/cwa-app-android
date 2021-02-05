package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.ui.submission.symptoms.introduction.SubmissionSymptomIntroductionFragment
import de.rki.coronawarnapp.ui.submission.symptoms.introduction.SubmissionSymptomIntroductionViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.takeScreenshot
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.captureScreenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class SubmissionSymptomIntroFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private lateinit var viewModel: SubmissionSymptomIntroductionViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            SubmissionSymptomIntroductionViewModel(
                TestDispatcherProvider(),
                submissionRepository,
                autoSubmission
            )
        )
        with(viewModel) {
            every { symptomIndication } returns MutableLiveData(Symptoms.Indication.POSITIVE)
        }
        setupMockViewModel(
            object : SubmissionSymptomIntroductionViewModel.Factory {
                override fun create(): SubmissionSymptomIntroductionViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
        unmockkAll()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<SubmissionSymptomIntroductionFragment>()
    }

    @Test
    fun testSymptomNextClicked() {
        launchFragmentInContainer2<SubmissionSymptomIntroductionFragment>()
        onView(withId(R.id.symptom_button_next))
            .perform(click())
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        captureScreenshot<SubmissionSymptomIntroductionFragment>()
        onView(withId(R.id.target_button_verify))
            .perform(scrollTo())
        takeScreenshot<SubmissionSymptomIntroductionFragment>("2")
    }
}

@Module
abstract class SubmissionSymptomIntroFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun submissionSymptomIntroScreen(): SubmissionSymptomIntroductionFragment
}
