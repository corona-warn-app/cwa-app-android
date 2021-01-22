package de.rki.coronawarnapp.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionStateProvider
import de.rki.coronawarnapp.submission.ui.homecards.TestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.TestResultItem
import de.rki.coronawarnapp.submission.ui.homecards.TestSubmissionDoneCard
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.states.TracingStateProvider
import de.rki.coronawarnapp.tracing.ui.homecards.TracingStateItem
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState
import de.rki.coronawarnapp.ui.main.home.items.DiaryCard
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.spyk
import io.mockk.verify
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
import testhelpers.launchFragment2
import testhelpers.launchFragmentInContainer2
import testhelpers.recyclerScrollTo
import timber.log.Timber
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest : BaseUITest() {

    @MockK lateinit var errorResetTool: EncryptionErrorResetTool
    @MockK lateinit var tracingStatus: GeneralTracingStatus
    @MockK lateinit var tracingStateProviderFactory: TracingStateProvider.Factory
    @MockK lateinit var submissionStateProvider: SubmissionStateProvider
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var shareTestResultNotificationService: ShareTestResultNotificationService
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var statisticsProvider: StatisticsProvider

    private lateinit var viewModel: HomeFragmentViewModel

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        viewModel = homeFragmentViewModelSpy()
        with(viewModel) {
            every { observeTestResultToSchedulePositiveTestResultReminder() } just Runs
            every { refreshRequiredData() } just Runs
            every { tracingHeaderState } returns MutableLiveData(TracingHeaderState.TracingActive)
            every { showLoweredRiskLevelDialog } returns MutableLiveData()
            every { homeItems } returns MutableLiveData(emptyList())
            every { popupEvents } returns SingleLiveEvent()
        }

        setupMockViewModel(
            object : HomeFragmentViewModel.Factory {
                override fun create(): HomeFragmentViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun onResumeCallsRefresh() {
        launchFragment2<HomeFragment>()
        verify(exactly = 1) { viewModel.refreshRequiredData() }
    }

    @Screenshot
    @Test
    fun capture_screenshot_low_risk() {
        every { viewModel.homeItems } returns itemsLiveData(
            HomeData.Tracing.LOW_RISK_ITEM
        )

        captureScreenshot("low_risk")
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo())
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        Screengrab.screenshot(HomeFragment::class.simpleName.plus("low_risk_2"))
    }

    @Screenshot
    @Test
    fun capture_screenshot_increased_risk() {
        every { viewModel.homeItems } returns itemsLiveData(
            HomeData.Tracing.INCREASED_RISK_ITEM
        )
        captureScreenshot("increased_risk")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_disabled() {
        every { viewModel.tracingHeaderState } returns MutableLiveData(TracingHeaderState.TracingInActive)
        every { viewModel.homeItems } returns itemsLiveData(
            HomeData.Tracing.TRACING_DISABLED_ITEM
        )
        captureScreenshot("tracing_disabled")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_progress_downloading() {
        every { viewModel.homeItems } returns itemsLiveData(
            HomeData.Tracing.TRACING_PROGRESS_ITEM
        )
        captureScreenshot("progress_downloading")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_failed() {
        every { viewModel.homeItems } returns itemsLiveData(
            HomeData.Tracing.TRACING_FAILED_ITEM
        )
        captureScreenshot("tracing_failed")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_submission_done() {
        every { viewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_SUBMISSION_DONE_ITEM
        )
        captureScreenshot("submission_done")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_error() {
        every { viewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_ERROR_ITEM
        )
        captureScreenshot("test_error")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_fetching() {
        every { viewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_FETCHING_ITEM
        )
        captureScreenshot("test_fetching")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_invalid() {
        every { viewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_INVALID_ITEM
        )
        captureScreenshot("test_invalid")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_negative() {
        every { viewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_NEGATIVE_ITEM
        )
        captureScreenshot("test_negative")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_positive() {
        every { viewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_POSITIVE_ITEM
        )
        captureScreenshot("test_positive")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_pending() {
        every { viewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_PENDING_ITEM
        )
        captureScreenshot("test_pending")
    }

    private fun captureScreenshot(nameSuffix: String) {
        val name = HomeFragment::class.simpleName + "_" + nameSuffix
        launchFragmentInContainer2<HomeFragment>()
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        Screengrab.screenshot(name)
    }

    private fun itemsLiveData(
        tracingStateItem: TracingStateItem = HomeData.Tracing.LOW_RISK_ITEM,
        submissionTestResultItem: TestResultItem = HomeData.Submission.TEST_UNREGISTERED_ITEM
    ): LiveData<List<HomeItem>> =
        MutableLiveData(
            mutableListOf<HomeItem>().apply {
                when (submissionTestResultItem) {
                    is TestSubmissionDoneCard.Item,
                    is TestPositiveCard.Item -> {
                        Timber.d("Tracing item is not added, submission:$submissionTestResultItem")
                    }
                    else -> add(tracingStateItem)
                }

                add(submissionTestResultItem)
                add(DiaryCard.Item {})
                add(FAQCard.Item {})
            }
        )

    private fun homeFragmentViewModelSpy() = spyk(
        HomeFragmentViewModel(
            dispatcherProvider = TestDispatcherProvider,
            errorResetTool = errorResetTool,
            tracingRepository = tracingRepository,
            tracingStateProviderFactory = tracingStateProviderFactory,
            shareTestResultNotificationService = shareTestResultNotificationService,
            appConfigProvider = appConfigProvider,
            tracingStatus = tracingStatus,
            submissionRepository = submissionRepository,
            submissionStateProvider = submissionStateProvider,
            cwaSettings = cwaSettings,
            statisticsProvider = statisticsProvider
        )
    )
}

@Module
abstract class HomeFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun homeScreen(): HomeFragment
}
