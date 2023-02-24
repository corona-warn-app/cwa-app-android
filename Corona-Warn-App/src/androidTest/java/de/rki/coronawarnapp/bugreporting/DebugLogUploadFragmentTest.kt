package de.rki.coronawarnapp.bugreporting

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.rki.coronawarnapp.bugreporting.debuglog.ui.upload.DebugLogUploadFragment
import de.rki.coronawarnapp.bugreporting.debuglog.ui.upload.DebugLogUploadViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@HiltAndroidTest
class DebugLogUploadFragmentTest : BaseUITest() {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val viewModel = mockk<DebugLogUploadViewModel>(relaxed = true)

    @Test
    fun launch_fragment() {
        launchFragmentInContainer2<DebugLogUploadFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<DebugLogUploadFragment>()
        takeScreenshot<DebugLogUploadFragment>()
    }
}
