package de.rki.coronawarnapp.ui.tracing.ui.details.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.tracing.ui.details.home.HomeRulesFragment
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class HomeRulesFragmentTest : BaseUITest() {

    @Screenshot
    @Test
    fun capture_screenshot() {
        captureScreenshot("home_rules")
    }

    private fun captureScreenshot(suffix: String) {
        launchFragmentInContainer2<HomeRulesFragment>()
        takeScreenshot<HomeRulesFragment>(suffix)
    }
}
