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
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionDispatcherFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDispatcherViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class SubmissionDispatcherFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionDispatcherViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(object : SubmissionDispatcherViewModel.Factory {
            override fun create(): SubmissionDispatcherViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionDispatcherFragment>()
    }

    @Test fun testEventQRClicked() {
        val scenario = launchFragmentInContainer<SubmissionDispatcherFragment>()
        onView(withId(R.id.submission_dispatcher_qr))
            .perform(scrollTo())
            .perform(click());
    }

    @Test fun testEventTeleClicked() {
        val scenario = launchFragmentInContainer<SubmissionDispatcherFragment>()
        onView(withId(R.id.submission_dispatcher_tan_tele))
            .perform(scrollTo())
            .perform(click());
    }

    @Test fun testEventTanClicked() {
        val scenario = launchFragmentInContainer<SubmissionDispatcherFragment>()
        onView(withId(R.id.submission_dispatcher_tan_code))
            .perform(scrollTo())
            .perform(click());
    }
}

@Module
abstract class SubmissionDispatcherTestModule {
    @ContributesAndroidInjector
    abstract fun submissionDispatcherScreen(): SubmissionDispatcherFragment
}
