package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
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
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class SubmissionSymptomIntroFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var dispatcherProvider: DispatcherProvider
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
        every { dispatcherProvider.Default } returns Dispatchers.Default
        viewModel =
            spyk(SubmissionSymptomIntroductionViewModel(dispatcherProvider, submissionRepository, autoSubmission))
        with(viewModel) {
            every { symptomIndication } returns MutableLiveData(Symptoms.Indication.POSITIVE)
        }
        setupMockViewModel(object : SubmissionSymptomIntroductionViewModel.Factory {
            override fun create(): SubmissionSymptomIntroductionViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionSymptomIntroductionFragment>()
    }

    @Test
    fun testSymptomNextClicked() {
        val scenario = launchFragmentInContainer<SubmissionSymptomIntroductionFragment>()
        onView(withId(R.id.symptom_button_next))
            .perform(scrollTo())
            .perform(click())

        // TODO verify result
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        captureScreenshot<SubmissionSymptomIntroductionFragment>()
        onView(withId(R.id.symptom_button_next))
            .perform(scrollTo())
        Thread.sleep(2000)
        Screengrab.screenshot(SubmissionSymptomIntroductionFragment::class.simpleName.plus("2"))
    }
}

@Module
abstract class SubmissionSymptomIntroFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun submissionSymptomIntroScreen(): SubmissionSymptomIntroductionFragment
}
