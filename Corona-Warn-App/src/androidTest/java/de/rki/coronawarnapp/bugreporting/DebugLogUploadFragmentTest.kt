package de.rki.coronawarnapp.bugreporting

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.bugreporting.debuglog.ui.upload.DebugLogUploadFragment
import de.rki.coronawarnapp.bugreporting.debuglog.ui.upload.DebugLogUploadViewModel
import de.rki.coronawarnapp.bugreporting.debuglog.upload.SnapshotUploader
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class DebugLogUploadFragmentTest : BaseUITest() {

    @MockK lateinit var snapShotUploader: SnapshotUploader

    private lateinit var viewModel: DebugLogUploadViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            DebugLogUploadViewModel(
                TestDispatcherProvider(),
                snapShotUploader
            )
        )
    }

    @Test
    fun launch_fragment() {
        launchFragment2<DebugLogUploadFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<DebugLogUploadFragment>()
        takeScreenshot<DebugLogUploadFragment>()
    }
}
