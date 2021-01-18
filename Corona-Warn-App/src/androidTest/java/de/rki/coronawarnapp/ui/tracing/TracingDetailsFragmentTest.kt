package de.rki.coronawarnapp.ui.tracing

import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.states.TracingStateProvider
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsFragment
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsFragmentViewModel
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsItemProvider
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsState
import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.SCREENSHOT_DELAY_TIME
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class TracingDetailsFragmentTest : BaseUITest() {

    @MockK lateinit var tracingStatus: GeneralTracingStatus
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var tracingDetailsItemProvider: TracingDetailsItemProvider
    @MockK lateinit var tracingStateProviderFactory: TracingStateProvider.Factory
    @MockK lateinit var tracingRepository: TracingRepository

    private lateinit var viewModel: TracingDetailsFragmentViewModel

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = spyk(
            TracingDetailsFragmentViewModel(
                dispatcherProvider = TestDispatcherProvider,
                tracingStatus = tracingStatus,
                backgroundModeStatus = backgroundModeStatus,
                riskLevelStorage = riskLevelStorage,
                tracingDetailsItemProvider = tracingDetailsItemProvider,
                tracingStateProviderFactory = tracingStateProviderFactory,
                tracingRepository = tracingRepository
            )
        )

        setupMockViewModel(
            object : TracingDetailsFragmentViewModel.Factory {
                override fun create(): TracingDetailsFragmentViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_tracing_details() {
        launchFragment<TracingDetailsFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_low_risk() {
        mockData(TracingData.LOW_RISK)
        captureScreenshot("tracing_low_risk")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_disabled() {
        mockData(TracingData.TRACING_DISABLED)
        captureScreenshot("tracing_disabled")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_failed() {
        mockData(TracingData.TRACING_FAILED)
        captureScreenshot("tracing_failed")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_increased() {
        mockData(TracingData.INCREASED_RISK)
        captureScreenshot("tracing_increased")
    }

    private fun mockData(pair: Pair<TracingDetailsState, List<DetailsItem>>) {
        every { viewModel.buttonStates } returns MutableLiveData(pair.first)
        every { viewModel.detailsItems } returns MutableLiveData(pair.second)
    }

    private fun captureScreenshot(nameSuffix: String) {
        val name = TracingDetailsFragment::class.simpleName + "_" + nameSuffix
        launchFragmentInContainer2<TracingDetailsFragment>()
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        Screengrab.screenshot(name)
    }
}

@Module
abstract class TracingDetailsFragmentTestTestModule {
    @ContributesAndroidInjector
    abstract fun tracingDetailsFragment(): TracingDetailsFragment
}
