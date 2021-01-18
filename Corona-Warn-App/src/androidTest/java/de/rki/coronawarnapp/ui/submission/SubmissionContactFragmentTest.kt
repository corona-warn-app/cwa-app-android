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
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionContactFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionContactViewModel
import io.mockk.MockKAnnotations
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.captureScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class SubmissionContactFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    private fun createViewModel() = SubmissionContactViewModel()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(object : SubmissionContactViewModel.Factory {
            override fun create(): SubmissionContactViewModel = createViewModel()
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionContactFragment>()
    }

    @Test
    fun testContactCallClicked() {
        val scenario = launchFragmentInContainer<SubmissionContactFragment>()
        onView(withId(R.id.submission_contact_button_call))
            .perform(click())

        // TODO verify result
    }

    @Test
    fun testContactEnterTanClicked() {
        val scenario = launchFragmentInContainer<SubmissionContactFragment>()
        onView(withId(R.id.submission_contact_button_enter))
            .perform(click())

        // TODO verify result
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        captureScreenshot<SubmissionContactFragment>()
    }
}

@Module
abstract class SubmissionContactTestModule {
    @ContributesAndroidInjector
    abstract fun submissionContactScreen(): SubmissionContactFragment
}
