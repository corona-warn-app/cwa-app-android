package de.rki.coronawarnapp.bugreporting

import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.rki.coronawarnapp.bugreporting.debuglog.ui.DebugLogFragment
import de.rki.coronawarnapp.bugreporting.debuglog.ui.DebugLogViewModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@HiltAndroidTest
class DebugLogFragmentTest : BaseUITest() {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val viewModel = mockk<DebugLogViewModel>(relaxed = true)

    @Before
    fun setup() {
        every { viewModel.state } returns mutableLiveData()
    }

    @Test
    fun launch_fragment() {
        launchFragmentInContainer2<DebugLogFragment>()
    }

    @Screenshot
    @Test
    fun capture_inactive_screenshot() {
        every { viewModel.state } returns mutableLiveData(false)
        launchFragmentInContainer2<DebugLogFragment>()
        takeScreenshot<DebugLogFragment>()
    }

    @Screenshot
    @Test
    fun capture_active_screenshot() {
        launchFragmentInContainer2<DebugLogFragment>()
        takeScreenshot<DebugLogFragment>()
    }

    private fun mutableLiveData(active: Boolean = true) = MutableLiveData(
        DebugLogViewModel.State(
            isRecording = active,
            currentSize = 12,
            isLowStorage = false,
            isActionInProgress = false
        )
    )
}
