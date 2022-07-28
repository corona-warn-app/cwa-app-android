package de.rki.coronawarnapp.ui.tracing.ui.details.hygiene

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.tracing.ui.details.hygiene.HygieneRulesFragment
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class HygieneRulesFragmentTest : BaseUITest() {

    @Screenshot
    @Test
    fun capture_screenshot() {
        captureScreenshot("hygiene_rules")
    }

    private fun captureScreenshot(suffix: String) {
        launchFragmentInContainer2<HygieneRulesFragment>()
        takeScreenshot<HygieneRulesFragment>(suffix)
    }
}
