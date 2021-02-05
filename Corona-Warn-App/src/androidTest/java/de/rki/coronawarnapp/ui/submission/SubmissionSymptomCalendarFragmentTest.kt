package de.rki.coronawarnapp.ui.submission

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
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
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class SubmissionSymptomCalendarFragmentTest : BaseUITest() {

    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var autoSubmission: AutoSubmission

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private lateinit var viewModel: SubmissionSymptomCalendarViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            SubmissionSymptomCalendarViewModel(
                Symptoms.Indication.POSITIVE,
                TestDispatcherProvider(),
                submissionRepository,
                autoSubmission
            )
        )
        with(viewModel) {
            every { symptomStart } returns MutableLiveData(Symptoms.StartOf.LastSevenDays)
        }
        setupMockViewModel(object : SubmissionSymptomCalendarViewModel.Factory {
            override fun create(symptomIndication: Symptoms.Indication): SubmissionSymptomCalendarViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
        unmockkAll()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        captureScreenshot<SubmissionSymptomCalendarFragment>(
            fragmentArgs = SubmissionSymptomCalendarFragmentArgs(
                Symptoms.Indication.POSITIVE
            ).toBundle()
        )

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
