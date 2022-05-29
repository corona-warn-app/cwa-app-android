package de.rki.coronawarnapp.ui.tracing

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.datadonation.survey.Surveys
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
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.recyclerScrollTo
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class TracingDetailsFragmentTest : BaseUITest() {

    @MockK lateinit var tracingStatus: GeneralTracingStatus
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var tracingDetailsItemProvider: TracingDetailsItemProvider
    @MockK lateinit var tracingStateProviderFactory: TracingStateProvider.Factory
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var surveys: Surveys

    private lateinit var viewModel: TracingDetailsFragmentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = spyk(
            TracingDetailsFragmentViewModel(
                dispatcherProvider = TestDispatcherProvider(),
                tracingStatus = tracingStatus,
                backgroundModeStatus = backgroundModeStatus,
                riskLevelStorage = riskLevelStorage,
                tracingDetailsItemProvider = tracingDetailsItemProvider,
                tracingStateProviderFactory = tracingStateProviderFactory,
                tracingRepository = tracingRepository,
                surveys = surveys
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
        launchFragment2<TracingDetailsFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_low_risk() {
        mockData(TracingData.LOW_RISK)
        captureScreenshot("tracing_low_risk")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_low_risk_with_one_encounter() {
        mockData(TracingData.LOW_RISK_WITH_ONE_ENCOUNTER)
        captureScreenshot("tracing_low_risk_with_one_encounters_1")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(3))
        takeScreenshot<TracingDetailsFragment>("tracing_low_risk_with_one_encounters_2")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(5))
        takeScreenshot<TracingDetailsFragment>("tracing_low_risk_with_one_encounters_3")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(5, additionalY = 800))
        takeScreenshot<TracingDetailsFragment>("tracing_low_risk_with_one_encounters_4")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_low_risk_with_two_encounters() {
        mockData(TracingData.LOW_RISK_WITH_TWO_ENCOUNTERS)
        captureScreenshot("tracing_low_risk_with_two_encounters_1")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(3))
        takeScreenshot<TracingDetailsFragment>("tracing_low_risk_with_two_encounters_2")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(5))
        takeScreenshot<TracingDetailsFragment>("tracing_low_risk_with_two_encounters_3")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(5, additionalY = 1000))
        takeScreenshot<TracingDetailsFragment>("tracing_low_risk_with_two_encounters_4")
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
        captureScreenshot("tracing_increased_1")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(2))
        takeScreenshot<TracingDetailsFragment>("tracing_increased_2")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(3))
        takeScreenshot<TracingDetailsFragment>("tracing_increased_3")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(5))
        takeScreenshot<TracingDetailsFragment>("tracing_increased_4")

        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(5, additionalY = 1000))
        takeScreenshot<TracingDetailsFragment>("tracing_increased_5")
    }

    private fun mockData(pair: Pair<TracingDetailsState, List<DetailsItem>>) {
        every { viewModel.buttonStates } returns MutableLiveData(pair.first)
        every { viewModel.detailsItems } returns MutableLiveData(pair.second)
    }

    private fun captureScreenshot(suffix: String) {
        launchFragmentInContainer2<TracingDetailsFragment>()
        takeScreenshot<TracingDetailsFragment>(suffix)
    }
}

@Module
abstract class TracingDetailsFragmentTestTestModule {
    @ContributesAndroidInjector
    abstract fun tracingDetailsFragment(): TracingDetailsFragment
}
