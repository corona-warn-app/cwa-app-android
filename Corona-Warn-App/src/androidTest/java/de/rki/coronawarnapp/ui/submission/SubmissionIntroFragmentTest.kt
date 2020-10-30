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
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionIntroFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionIntroViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class SubmissionIntroFragmentTest : BaseUITest() {

    /*@get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java)*/

    @Rule @JvmField
    val localeTestRule = LocaleTestRule()

    @MockK lateinit var viewModel: SubmissionIntroViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(object : SubmissionIntroViewModel.Factory {
            override fun create(): SubmissionIntroViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<SubmissionIntroFragment>()
        Screengrab.screenshot("submission_Intro_fragment_opened")
    }

    @Test fun testEventNextClicked() {
        val scenario = launchFragmentInContainer<SubmissionIntroFragment>()
        onView(withId(R.id.submission_intro_button_next))
            .perform(click())

        // TODO verify result
    }
}

@Module
abstract class SubmissionIntroTestModule {
    @ContributesAndroidInjector
    abstract fun submissionIntroScreen(): SubmissionIntroFragment
}
