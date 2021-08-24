package de.rki.coronawarnapp.ui.presencetracing.organizer.warn

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class TraceLocationWarnInfoFragmentTest : BaseUITest() {

    @Screenshot
    @Test
    fun screenshot() {
        launchFragmentInContainer2<TraceLocationWarnInfoFragment>()
        takeScreenshot<TraceLocationWarnInfoFragment>()
    }
}
