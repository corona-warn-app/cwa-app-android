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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.SCREENSHOT_DELAY_TIME
import testhelpers.ScreenShotter
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.captureScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class SubmissionDispatcherFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private fun createViewModel() = SubmissionDispatcherViewModel()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(object : SubmissionDispatcherViewModel.Factory {
            override fun create(): SubmissionDispatcherViewModel = createViewModel()
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

    @Test
    fun testEventQRClicked() {
        launchFragmentInContainer<SubmissionDispatcherFragment>()
        onView(withId(R.id.submission_dispatcher_qr))
            .perform(scrollTo())
            .perform(click())
    }

    @Test
    fun testEventTeleClicked() {
        launchFragmentInContainer<SubmissionDispatcherFragment>()
        onView(withId(R.id.submission_dispatcher_tan_tele))
            .perform(scrollTo())
            .perform(click())
    }

    @Test
    fun testEventTanClicked() {
        launchFragmentInContainer<SubmissionDispatcherFragment>()
        onView(withId(R.id.submission_dispatcher_tan_code))
            .perform(scrollTo())
            .perform(click())
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        captureScreenshot<SubmissionDispatcherFragment>()
        onView(withId(R.id.submission_dispatcher_tan_tele))
            .perform(scrollTo())
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        ScreenShotter.capture<SubmissionDispatcherFragment>("2")
    }
}

@Module
abstract class SubmissionDispatcherTestModule {
    @ContributesAndroidInjector
    abstract fun submissionDispatcherScreen(): SubmissionDispatcherFragment
}
