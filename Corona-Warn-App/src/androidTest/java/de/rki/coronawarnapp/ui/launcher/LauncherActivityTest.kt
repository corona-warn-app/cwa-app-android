package de.rki.coronawarnapp.ui.launcher

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class LauncherActivityTest : BaseUITest() {

    @RelaxedMockK lateinit var viewModel: LauncherActivityViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }



    @Test
    fun testDeepLinkLowercase() {
        val uri = Uri.parse("https://e.coronawarn.app/c1/SOME_PATH_GOES_HERE")
        launchActivity<LauncherActivity>(getIntent(uri))
    }

    @Test(expected = RuntimeException::class)
    fun testDeepLinkDoNotOpenOtherLinks() {
        val uri = Uri.parse("https://www.rki.de")
        launchActivity<LauncherActivity>(getIntent(uri))
    }

    @Test(expected = RuntimeException::class)
    fun testDeepLinkUppercase() {
        // Host is case sensitive and it should be only in lowercase
        val uri = Uri.parse("HTTPS://CORONAWARN.APP/E1/SOME_PATH_GOES_HERE")
        launchActivity<LauncherActivity>(getIntent(uri))
    }

    @Screenshot
    @Test
    fun capture_root_dialog_screenshot() {
        every { viewModel.events } returns SingleLiveEvent<LauncherEvent>().also {
            it.postValue(LauncherEvent.ShowRootedDialog)
        }

        launchActivity<LauncherActivity>()
        takeScreenshot<LauncherActivity>("launcher_root")
    }

    private fun getIntent(uri: Uri) = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage(InstrumentationRegistry.getInstrumentation().targetContext.packageName)
        addCategory(Intent.CATEGORY_BROWSABLE)
        addCategory(Intent.CATEGORY_DEFAULT)
    }
}
