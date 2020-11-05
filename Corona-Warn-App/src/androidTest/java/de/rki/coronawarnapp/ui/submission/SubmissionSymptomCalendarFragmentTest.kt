package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.symptoms.calendar.SubmissionSymptomCalendarFragment
import de.rki.coronawarnapp.ui.submission.symptoms.calendar.SubmissionSymptomCalendarViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class SubmissionSymptomCalendarFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionSymptomCalendarViewModel

    /*  private val symptoms = Symptoms(Symptoms.StartOf.OneToTwoWeeksAgo, POSITIVE)
      private val positiveSymptomIndication = POSITIVE;
      private val negativeSymptomIndication = Symptoms.Indication.NEGATIVE;
      private val noSymptomIndication = Symptoms.Indication.NO_INFORMATION;*/

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : SubmissionSymptomCalendarViewModel.Factory {
            override fun create(symptomIndication: Symptoms.Indication): SubmissionSymptomCalendarViewModel =
                viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionSymptomCalendarFragment>()
    }

    @Test fun testSymptomCalendarNextClicked() {
        val scenario = launchFragmentInContainer<SubmissionSymptomCalendarFragment>()
        onView(withId(R.id.symptom_button_next))
            .perform(scrollTo())
            .perform(click())

        // TODO verify result
    }
}

@Module
abstract class SubmissionSymptomCalendarFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun submissionSymptomCalendarScreen(): SubmissionSymptomCalendarFragment
}
