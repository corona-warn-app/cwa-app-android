package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionDoneFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDoneViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class SubmissionDoneFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionDoneViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(object : SubmissionDoneViewModel.Factory {
            override fun create(): SubmissionDoneViewModel = viewModel
        })

    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionDoneFragment>()
    }

    @Test fun testDoneClicked() {
        val scenario = launchFragmentInContainer<SubmissionDoneFragment>()
        onView(withId(R.id.submission_done_button_done))
            .perform(click())

        //ToDo verify result

    }
}

@Module
abstract class SubmissionDoneTestModule {
    @ContributesAndroidInjector
    abstract fun submissionDoneScreen(): SubmissionDoneFragment
}


