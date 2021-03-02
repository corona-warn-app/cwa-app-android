package de.rki.coronawarnapp.ui.launcher

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.update.UpdateChecker
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.spyk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.TestDispatcherProvider

@RunWith(AndroidJUnit4::class)
class LauncherActivityTest : BaseUITest() {

    @MockK lateinit var updateChecker: UpdateChecker
    @MockK lateinit var cwaSettings: CWASettings
    lateinit var viewModel: LauncherActivityViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(LocalData)
        mockkObject(OnboardingActivity)

        coEvery { updateChecker.checkForUpdate() } returns UpdateChecker.Result(isUpdateNeeded = false)
        every { LocalData.isOnboarded() } returns false
        every { OnboardingActivity.start(any()) } just Runs

        viewModel = launcherActivityViewModel()
        setupMockViewModel(
            object : LauncherActivityViewModel.Factory {
                override fun create(): LauncherActivityViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun testDeepLinkLowercase() {
        val uri = Uri.parse("https://coronawarn.app/E1/SOME_PATH_GOES_HERE")
        launchActivity<LauncherActivity>(getIntent(uri))
    }

    @Test
    fun testDeepLinkLowercaseWww() {
        val uri = Uri.parse("https://www.coronawarn.app/E1/SOME_PATH_GOES_HERE")
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
            updateChecker,
            TestDispatcherProvider(),
            cwaSettings
        )
    )
}

@Module
abstract class LauncherActivityTestModule {
    @ContributesAndroidInjector
    abstract fun launcherActivity(): LauncherActivity
}
