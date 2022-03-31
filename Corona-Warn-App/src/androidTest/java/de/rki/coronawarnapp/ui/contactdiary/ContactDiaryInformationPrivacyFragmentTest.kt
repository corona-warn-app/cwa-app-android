package de.rki.coronawarnapp.ui.contactdiary

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.information.InformationPrivacyFragment
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.takeScreenshot
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.setViewVisibility

@RunWith(AndroidJUnit4::class)
class ContactDiaryInformationPrivacyFragmentTest : BaseUITest() {

    @Test
    fun launch_fragment() {
        launchFragment2<InformationPrivacyFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<InformationPrivacyFragment>()
        Espresso.onView(ViewMatchers.withId(R.id.information_privacy_container)).perform(setViewVisibility(true))
        takeScreenshot<InformationPrivacyFragment>()
    }
}
