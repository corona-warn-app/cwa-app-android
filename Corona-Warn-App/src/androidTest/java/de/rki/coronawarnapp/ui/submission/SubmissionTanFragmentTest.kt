package de.rki.coronawarnapp.ui.submission

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.FlakyTest
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.tan.SubmissionTanFragment
import de.rki.coronawarnapp.ui.submission.tan.SubmissionTanViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
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
class SubmissionTanFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionTanViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        setupMockViewModel(
            object : SubmissionTanViewModel.Factory {
                override fun create(): SubmissionTanViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<SubmissionTanFragment>()
    }

    @Test
    @FlakyTest
    fun testEventTanNextClicked() {
        launchFragmentInContainer2<SubmissionTanFragment>()
        closeSoftKeyboard()
        onView(withId(R.id.submission_tan_button_enter))
            .perform(click())
    }

    @Test
    @Screenshot
    fun capture_fragment_empty() {
        launchFragmentInContainer2<SubmissionTanFragment>()
        onView(withId(R.id.tan_input_edittext))
            .perform(click())
            .perform(closeSoftKeyboard())
        takeScreenshot<SubmissionTanFragment>()
    }

    @Test
    @Screenshot
    fun capture_fragment_done() {
        launchFragmentInContainer2<SubmissionTanFragment>()
        onView(withId(R.id.tan_input_edittext))
            .perform(click())
            .perform(typeText("AC9UHD65AF"), closeSoftKeyboard())
        takeScreenshot<SubmissionTanFragment>("done")
    }

    @Test
    @Screenshot
    fun capture_fragment_invalid() {
        launchFragmentInContainer2<SubmissionTanFragment>()
        onView(withId(R.id.tan_input_edittext))
            .perform(click())
            .perform(typeText("AC9U0"), closeSoftKeyboard())
        takeScreenshot<SubmissionTanFragment>("invalid")
    }
}

@Module
abstract class SubmissionTanTestModule {
    @ContributesAndroidInjector
    abstract fun submissionTanScreen(): SubmissionTanFragment
}
