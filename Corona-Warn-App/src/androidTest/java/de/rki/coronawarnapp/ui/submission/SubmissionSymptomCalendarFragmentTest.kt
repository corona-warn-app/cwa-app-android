package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
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
import de.rki.coronawarnapp.ui.submission.symptoms.calendar.SubmissionSymptomCalendarFragment
import de.rki.coronawarnapp.ui.submission.symptoms.calendar.SubmissionSymptomCalendarFragmentArgs
import de.rki.coronawarnapp.ui.submission.symptoms.calendar.SubmissionSymptomCalendarViewModel
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
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class SubmissionSymptomCalendarFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector

    private lateinit var viewModel: SubmissionSymptomCalendarViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            SubmissionSymptomCalendarViewModel(
                Symptoms.Indication.POSITIVE,
                BaseCoronaTest.Type.PCR,
                TestDispatcherProvider(),
                submissionRepository,
                autoSubmission,
                analyticsKeySubmissionCollector,
                TestScope()
            )
        )
        with(viewModel) {
            every { symptomStart } returns MutableLiveData(Symptoms.StartOf.LastSevenDays)
        }
        setupMockViewModel(
            object : SubmissionSymptomCalendarViewModel.Factory {
                override fun create(
                    symptomIndication: Symptoms.Indication,
                    testType: BaseCoronaTest.Type
                ): SubmissionSymptomCalendarViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<SubmissionSymptomCalendarFragment>(
            fragmentArgs = SubmissionSymptomCalendarFragmentArgs(
                Symptoms.Indication.POSITIVE,
                BaseCoronaTest.Type.PCR
            ).toBundle()
        )

        takeScreenshot<SubmissionSymptomCalendarFragment>()

        onView(withId(R.id.target_button_verify))
            .perform(scrollTo())

        takeScreenshot<SubmissionSymptomCalendarFragment>("2")
    }
}

@Module
abstract class SubmissionSymptomCalendarFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun submissionSymptomIntroScreen(): SubmissionSymptomCalendarFragment
}
