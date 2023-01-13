package de.rki.coronawarnapp.srs.ui.done

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class SrsSubmissionDoneFragmentTest : BaseUITest() {

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<SrsSubmissionDoneFragment>()
        takeScreenshot<SrsSubmissionDoneFragment>()

        onView(withId(R.id.further_info_text)).perform(ViewActions.scrollTo())
        takeScreenshot<SrsSubmissionDoneFragment>("1")
    }
}

@Module
abstract class SrsSubmissionDoneFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun srsSubmissionDoneScreen(): SrsSubmissionDoneFragment
}
