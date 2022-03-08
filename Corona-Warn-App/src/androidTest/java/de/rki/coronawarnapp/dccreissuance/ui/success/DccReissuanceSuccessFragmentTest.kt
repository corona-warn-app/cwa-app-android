package de.rki.coronawarnapp.dccreissuance.ui.success

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class DccReissuanceSuccessFragmentTest : BaseUITest() {

    @Test
    fun launch_fragment() {
        launchFragment2<DccReissuanceSuccessFragment>()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<DccReissuanceSuccessFragment>()
        takeScreenshot<DccReissuanceSuccessFragment>()
    }
}
