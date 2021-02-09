package de.rki.coronawarnapp.ui.submission

import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionDispatcherFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDispatcherViewModel
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.takeScreenshot
import testhelpers.captureScreenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class SubmissionDispatcherFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private fun createViewModel() = SubmissionDispatcherViewModel()

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        runOnUiThread { setGraph(R.navigation.nav_graph) }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : SubmissionDispatcherViewModel.Factory {
                override fun create(): SubmissionDispatcherViewModel = createViewModel()
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
        unmockkAll()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<SubmissionDispatcherFragment>()
    }

    @Test
    fun testEventQRClicked() {
        launchFragmentInContainer2<SubmissionDispatcherFragment>().onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
        onView(withId(R.id.submission_dispatcher_qr))
            .perform(scrollTo())
            .perform(click())
    }

    @Test
    fun testEventTeleClicked() {
        launchFragmentInContainer2<SubmissionDispatcherFragment>().onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
        onView(withId(R.id.submission_dispatcher_tan_tele))
            .perform(scrollTo())
            .perform(click())
    }

    @Test
    fun testEventTanClicked() {
        launchFragmentInContainer2<SubmissionDispatcherFragment>().onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
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
        takeScreenshot<SubmissionDispatcherFragment>("2")
    }
}

@Module
abstract class SubmissionDispatcherTestModule {
    @ContributesAndroidInjector
    abstract fun submissionDispatcherScreen(): SubmissionDispatcherFragment
}
