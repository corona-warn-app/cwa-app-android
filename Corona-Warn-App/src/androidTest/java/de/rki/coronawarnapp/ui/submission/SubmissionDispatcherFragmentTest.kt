package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.profile.storage.ProfileSettingsDataStore
import de.rki.coronawarnapp.srs.core.SrsLocalChecker
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionDispatcherFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionDispatcherViewModel
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class SubmissionDispatcherFragmentTest : BaseUITest() {

    @MockK lateinit var profileSettings: ProfileSettingsDataStore
    @MockK lateinit var srsLocalChecker: SrsLocalChecker

    private fun createViewModel() = SubmissionDispatcherViewModel(
        profileSettings = profileSettings,
        dispatcherProvider = TestDispatcherProvider(),
        srsLocalChecker = srsLocalChecker
    )

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        runOnUiThread {
            setGraph(R.navigation.nav_graph)
            setCurrentDestination(R.id.submissionDispatcherFragment)
        }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { profileSettings.profileFlow } returns flowOf(null)
        coEvery { srsLocalChecker.check() } just Runs
    }



    @Test
    fun launch_fragment() {
        launchFragment2<SubmissionDispatcherFragment>()
    }

    @Test
    fun testEventQRClicked() {
        launchFragmentInContainer<SubmissionDispatcherFragment>(themeResId = R.style.AppTheme_Main)
            .onFragment {
                Navigation.setViewNavController(it.requireView(), navController)
            }
        onView(withId(R.id.submission_dispatcher_qr))
            .perform(scrollTo())
            .perform(click())
    }

    @Ignore("Removed tile from UI, not deleting it completely for now")
    @Test
    fun testEventTeleClicked() {
        launchFragmentInContainer<SubmissionDispatcherFragment>(themeResId = R.style.AppTheme_Main)
            .onFragment {
                Navigation.setViewNavController(it.requireView(), navController)
            }
        onView(withId(R.id.submission_dispatcher_tan_tele))
            .perform(scrollTo())
            .perform(click())
    }

    @Ignore("Removed tile from UI, not deleting it completely for now")
    @Test
    fun testEventTanClicked() {
        launchFragmentInContainer<SubmissionDispatcherFragment>(themeResId = R.style.AppTheme_Main)
            .onFragment {
                Navigation.setViewNavController(it.requireView(), navController)
            }
        onView(withId(R.id.submission_dispatcher_tan_code))
            .perform(scrollTo())
            .perform(click())
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<SubmissionDispatcherFragment>()
        takeScreenshot<SubmissionDispatcherFragment>()

        onView(withId(R.id.submission_dispatcher_qr)).perform(scrollTo())
        takeScreenshot<SubmissionDispatcherFragment>("1")

        onView(withId(R.id.profile_card)).perform(scrollTo())
        takeScreenshot<SubmissionDispatcherFragment>("2")
    }
}
