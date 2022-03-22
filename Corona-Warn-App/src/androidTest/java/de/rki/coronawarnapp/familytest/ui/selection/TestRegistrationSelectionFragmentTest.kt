package de.rki.coronawarnapp.familytest.ui.selection

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class TestRegistrationSelectionFragmentTest : BaseUITest() {

    @Test
    @Screenshot
    fun capture_fragment() {
        launchFragmentInContainer2<TestRegistrationSelectionFragment>()
        takeScreenshot<TestRegistrationSelectionFragment>()
    }
}
