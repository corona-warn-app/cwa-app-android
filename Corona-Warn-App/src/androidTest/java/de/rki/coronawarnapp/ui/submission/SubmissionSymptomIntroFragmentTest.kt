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
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.ui.submission.symptoms.introduction.SubmissionSymptomIntroductionFragment
import de.rki.coronawarnapp.ui.submission.symptoms.introduction.SubmissionSymptomIntroductionFragmentArgs
import de.rki.coronawarnapp.ui.submission.symptoms.introduction.SubmissionSymptomIntroductionViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class SubmissionSymptomIntroFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector

    private lateinit var viewModel: SubmissionSymptomIntroductionViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            SubmissionSymptomIntroductionViewModel(
                dispatcherProvider = TestDispatcherProvider(),
                submissionRepository = submissionRepository,
                autoSubmission = autoSubmission,
                analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
                testType = BaseCoronaTest.Type.PCR,
                comesFromDispatcherFragment = false,
                appScope = TestScope()
            )
        )
        with(viewModel) {
            every { symptomIndication } returns MutableLiveData(Symptoms.Indication.POSITIVE)
        }
        setupMockViewModel(
            object : SubmissionSymptomIntroductionViewModel.Factory {
                override fun create(
                    testType: BaseCoronaTest.Type,
                    comesFromDispatcherFragment: Boolean
                ): SubmissionSymptomIntroductionViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<SubmissionSymptomIntroductionFragment>(
            fragmentArgs = SubmissionSymptomIntroductionFragmentArgs(
                BaseCoronaTest.Type.PCR
            ).toBundle()
        )
    }

    @Test
    fun testSymptomNextClicked() {
        launchFragmentInContainer2<SubmissionSymptomIntroductionFragment>(
            fragmentArgs = SubmissionSymptomIntroductionFragmentArgs(
                BaseCoronaTest.Type.PCR
            ).toBundle()
        )
        onView(withId(R.id.symptom_button_next))
            .perform(click())
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<SubmissionSymptomIntroductionFragment>(
            fragmentArgs = SubmissionSymptomIntroductionFragmentArgs(
                BaseCoronaTest.Type.PCR
            ).toBundle()
        )
        takeScreenshot<SubmissionSymptomIntroductionFragment>()
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
