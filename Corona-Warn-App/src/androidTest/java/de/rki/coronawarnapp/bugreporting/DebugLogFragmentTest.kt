package de.rki.coronawarnapp.bugreporting

import android.content.ContentResolver
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.bugreporting.debuglog.export.SAFLogExport
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
import de.rki.coronawarnapp.bugreporting.debuglog.ui.DebugLogFragment
import de.rki.coronawarnapp.bugreporting.debuglog.ui.DebugLogViewModel
import de.rki.coronawarnapp.nearby.ENFClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class DebugLogFragmentTest : BaseUITest() {

    @MockK lateinit var debugLogger: DebugLogger
    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var bugReportingSettings: BugReportingSettings
    @MockK lateinit var logSnapshotter: LogSnapshotter
    @MockK lateinit var safLogExport: SAFLogExport
    @MockK lateinit var contentResolver: ContentResolver

    private lateinit var viewModel: DebugLogViewModel

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {

        MockKAnnotations.init(this, relaxed = true)
        viewModel = spyk(
            DebugLogViewModel(
                debugLogger,
                TestDispatcherProvider(),
                enfClient,
                bugReportingSettings,
                logSnapshotter,
                safLogExport,
                contentResolver
            )
        )
        setupMockViewModel(
            object : DebugLogViewModel.Factory {
                override fun create(): DebugLogViewModel = viewModel
            }
        )
    }


    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment2<DebugLogFragment>()
    }

    @Screenshot
    @Test
    fun capture_inactive_screenshot() {
        launchFragmentInContainer2<DebugLogFragment>()
        takeScreenshot<DebugLogFragment>()
    }

    @Screenshot
    @Test
    fun capture_active_screenshot() {
        
        launchFragmentInContainer2<DebugLogFragment>()
        takeScreenshot<DebugLogFragment>()
    }
}


@Module
abstract class DebugLogTestModule {
    @ContributesAndroidInjector
    abstract fun debugLogFragment(): DebugLogFragment
}

