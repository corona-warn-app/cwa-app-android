package de.rki.coronawarnapp.ui.submission

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionContactFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionContactViewModel
import io.mockk.MockKAnnotations
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class SubmissionContactFragmentTest : BaseUITest() {

    private fun createViewModel() = SubmissionContactViewModel()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : SubmissionContactViewModel.Factory {
                override fun create(): SubmissionContactViewModel = createViewModel()
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<SubmissionContactFragment>()
    }

    @Test
    fun testContactEnterTanClicked() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        runOnUiThread {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.submissionContactFragment)
        }
        launchFragmentInContainer<SubmissionContactFragment>(themeResId = R.style.AppTheme_Main)
            .onFragment { fragment ->
                Navigation.setViewNavController(fragment.requireView(), navController)
            }

        onView(withId(R.id.submission_contact_button_enter))
            .perform(click())
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<SubmissionContactFragment>()
        takeScreenshot<SubmissionContactFragment>()
    }
}

@Module
abstract class SubmissionContactTestModule {
    @ContributesAndroidInjector
    abstract fun submissionContactScreen(): SubmissionContactFragment
}
