package de.rki.coronawarnapp.covidcertificate.covpass

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class CovPassInfoFragmentTest : BaseUITest() {

    @Test
    fun launch_fragment() {
        launchFragment2<CovPassInfoFragment>()
    }

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<CovPassInfoFragment>()
        takeScreenshot<CovPassInfoFragment>()
    }
}
