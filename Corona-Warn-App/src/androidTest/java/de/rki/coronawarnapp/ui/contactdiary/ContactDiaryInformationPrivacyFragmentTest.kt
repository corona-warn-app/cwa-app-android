package de.rki.coronawarnapp.ui.contactdiary

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.ui.information.InformationPrivacyFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.takeScreenshot
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2

@RunWith(AndroidJUnit4::class)
class ContactDiaryInformationPrivacyFragmentTest : BaseUITest() {

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Test
    fun launch_fragment() {
        launchFragment2<InformationPrivacyFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<InformationPrivacyFragment>()
        takeScreenshot<InformationPrivacyFragment>()
    }
}
