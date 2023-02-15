package de.rki.coronawarnapp.dccreissuance.ui.success

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@HiltAndroidTest
class DccReissuanceSuccessFragmentTest : BaseUITest() {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

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
