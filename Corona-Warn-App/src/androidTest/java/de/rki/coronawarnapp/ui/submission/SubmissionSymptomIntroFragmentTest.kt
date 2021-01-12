package de.rki.coronawarnapp.ui.submission

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.submission.symptoms.introduction.SubmissionSymptomIntroductionFragment
import de.rki.coronawarnapp.ui.submission.symptoms.introduction.SubmissionSymptomIntroductionViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class SubmissionSymptomIntroFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionSymptomIntroductionViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
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
//        launchFragmentInContainer2<SubmissionSymptomIntroductionFragment>()
    }

    @Test fun testSymptomNextClicked() {
//        val scenario = launchFragmentInContainer2<SubmissionSymptomIntroductionFragment>()
//        onView(withId(R.id.symptom_button_next))
//            .perform(scrollTo())
//            .perform(click())
//
//        // TODO verify result
    }
}

@Module
abstract class SubmissionSymptomIntroFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun submissionSymptomIntroScreen(): SubmissionSymptomIntroductionFragment
}
