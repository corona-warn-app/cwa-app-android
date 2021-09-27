package de.rki.coronawarnapp.ui.launcher

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.rootdetection.RootDetectionCheck
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.update.getUpdateInfo
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.TestDispatcherProvider

@RunWith(AndroidJUnit4::class)
class LauncherActivityTest : BaseUITest() {

    @MockK lateinit var appUpdateManager: AppUpdateManager
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var rootDetectionCheck: RootDetectionCheck
    lateinit var viewModel: LauncherActivityViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic("de.rki.coronawarnapp.update.InAppUpdateKt")

        coEvery { appUpdateManager.getUpdateInfo() } returns
            mockk<AppUpdateInfo>().apply {
                every { updateAvailability() } returns UpdateAvailability.UPDATE_NOT_AVAILABLE
            }

        coEvery { rootDetectionCheck.checkRoot() } returns false

        every { onboardingSettings.isOnboarded } returns false
        viewModel = launcherActivityViewModel()
        setupMockViewModel(
            object : LauncherActivityViewModel.Factory {
                override fun create(): LauncherActivityViewModel = viewModel
            }
        )

        every { viewModel.events } returns mockk<SingleLiveEvent<LauncherEvent>>().apply {
            every { observe(any(), any()) } just Runs
        }
    }

    @After
    fun teardown() {
        clearAllViewModels()
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

    private fun getIntent(uri: Uri) = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage(InstrumentationRegistry.getInstrumentation().targetContext.packageName)
        addCategory(Intent.CATEGORY_BROWSABLE)
        addCategory(Intent.CATEGORY_DEFAULT)
    }

    private fun launcherActivityViewModel() = spyk(
        LauncherActivityViewModel(
            appUpdateManager,
            TestDispatcherProvider(),
            cwaSettings,
            onboardingSettings,
            rootDetectionCheck
        )
    )
}

@Module
abstract class LauncherActivityTestModule {
    @ContributesAndroidInjector
    abstract fun launcherActivity(): LauncherActivity
}
