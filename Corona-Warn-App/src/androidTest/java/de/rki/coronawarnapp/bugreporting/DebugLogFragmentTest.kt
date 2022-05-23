package de.rki.coronawarnapp.bugreporting

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
import de.rki.coronawarnapp.bugreporting.debuglog.ui.DebugLogFragment
import de.rki.coronawarnapp.bugreporting.debuglog.ui.DebugLogViewModel
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage.UploadHistoryStorage
import de.rki.coronawarnapp.nearby.ENFClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import org.junit.After
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
class DebugLogFragmentTest : BaseUITest() {

    @MockK lateinit var debugLogger: DebugLogger
    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var uploadHistoryStorage: UploadHistoryStorage
    @MockK lateinit var logSnapshotter: LogSnapshotter

    private lateinit var inactiveViewModel: DebugLogViewModel
    private lateinit var activeViewModel: DebugLogViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        inactiveViewModel = setupViewModels(false, 0)
        activeViewModel = setupViewModels(true, 9410)

        setupMockViewModel(
            object : DebugLogViewModel.Factory {
                override fun create(): DebugLogViewModel = inactiveViewModel
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
        setupMockViewModel(
            object : DebugLogViewModel.Factory {
                override fun create(): DebugLogViewModel = activeViewModel
            }
        )
        launchFragmentInContainer2<DebugLogFragment>()
        takeScreenshot<DebugLogFragment>()
    }

    private fun setupViewModels(
        isRecording: Boolean,
        currentSize: Long,
        isLowStorage: Boolean = false,
        isActionInProgress: Boolean = false,
    ): DebugLogViewModel {
        val vm = spyk(
            DebugLogViewModel(
                debugLogger = debugLogger,
                dispatcherProvider = TestDispatcherProvider(),
                enfClient = enfClient,
                uploadHistoryStorage = uploadHistoryStorage,
                logSnapshotter = logSnapshotter
            )
        )
        with(vm) {
            every { state } returns MutableLiveData(
                DebugLogViewModel.State(
                    isRecording = isRecording,
                    currentSize = currentSize,
                    isLowStorage = isLowStorage,
                    isActionInProgress = isActionInProgress
                )
            )
        }
        return vm
    }
}

@Module
abstract class DebugLogTestModule {
    @ContributesAndroidInjector
    abstract fun debugLogFragment(): DebugLogFragment
}
