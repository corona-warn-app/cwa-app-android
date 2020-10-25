package de.rki.coronawarnapp.ui.submission

import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.warnothers.SubmissionResultPositiveOtherWarningFragment
import de.rki.coronawarnapp.ui.submission.warnothers.SubmissionResultPositiveOtherWarningViewModel
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
import de.rki.coronawarnapp.ui.submission.symptoms.calendar.SubmissionSymptomCalendarViewModel
import de.rki.coronawarnapp.ui.submission.symptoms.introduction.SubmissionSymptomIntroductionViewModel
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class SubmissionOtherWarningFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionResultPositiveOtherWarningViewModel
    @MockK lateinit var symptomIntroViewModel: SubmissionSymptomIntroductionViewModel
    @MockK lateinit var symptomCalendarViewModel: SubmissionSymptomCalendarViewModel

  //  @MockK lateinit var symptoms: Symptoms



    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(object : SubmissionSymptomIntroductionViewModel.Factory {
            override fun create(): SubmissionSymptomIntroductionViewModel = symptomIntroViewModel
        })

       /* every { symptomIntroViewModel.symptomIndication } returns MutableLiveData()
        every { symptomIntroViewModel. } returns MutableLiveData()

        symptomIntroViewModel.onPositiveSymptomIndication()
        symptomIntroViewModel.symptomIndication.postValue("tesasdf") */

        setupMockViewModel(object : SubmissionSymptomCalendarViewModel.Factory {
            override fun create(symptomIndication: Symptoms.Indication): SubmissionSymptomCalendarViewModel = symptomCalendarViewModel
        })

        setupMockViewModel(object : SubmissionResultPositiveOtherWarningViewModel.Factory {
            override fun create(symptoms: Symptoms): SubmissionResultPositiveOtherWarningViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionResultPositiveOtherWarningFragment>()
    }

    @Test fun testOtherWarningNextClicked() {
        val scenario = launchFragmentInContainer<SubmissionResultPositiveOtherWarningFragment>()
        onView(withId(R.id.submission_positive_other_warning_button_next))
            .perform(scrollTo())
            .perform(click());

        //ToDo verify result

    }
}

@Module
abstract class SubmissionOtherWarningTestModule {
    @ContributesAndroidInjector
    abstract fun submissionOtherWarningScreen(): SubmissionResultPositiveOtherWarningFragment
}
