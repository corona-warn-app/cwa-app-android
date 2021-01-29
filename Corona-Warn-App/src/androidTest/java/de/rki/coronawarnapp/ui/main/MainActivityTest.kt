package de.rki.coronawarnapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryWorkScheduler
import de.rki.coronawarnapp.contactdiary.ui.ContactDiarySettings
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.storage.LocalData
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
import de.rki.coronawarnapp.ui.main.home.HomeData
import de.rki.coronawarnapp.ui.main.home.HomeFragment
import de.rki.coronawarnapp.ui.main.home.HomeFragmentViewModel
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.device.PowerManagement
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
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
import testhelpers.recyclerScrollTo
import timber.log.Timber
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class MainActivityTest : BaseUITest() {
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
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var tracingStateProvider: TracingStateProvider
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var homeFragmentViewModel: HomeFragmentViewModel

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, true)
        mockkObject(LocalData)
        mockkObject(CWADebug)
        mockkObject(BackgroundWorkScheduler)
        // Common mocks
        every { CWADebug.isDeviceForTestersBuild } returns false
        every { environmentSetup.currentEnvironment } returns EnvironmentSetup.Type.PRODUCTION
        every { LocalData.isBackgroundCheckDone() } returns true
        every { LocalData.submissionWasSuccessful() } returns false
        every { BackgroundWorkScheduler.startWorkScheduler() } just Runs

        setupActivityViewModel()
        setupHomeFragmentViewModel()
    }

    @After
    fun teardown() {
        clearAllViewModels()
        unmockkAll()
    }

    @Test
    fun launchActivity() {
        launchActivity<MainActivity>()
    }

    @Screenshot
    @Test
    fun capture_screenshot_low_risk() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            HomeData.Tracing.LOW_RISK_ITEM
        )

        captureScreenshot("low_risk")
        Espresso.onView(ViewMatchers.withId(R.id.recycler_view)).perform(recyclerScrollTo())
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        Screengrab.screenshot(HomeFragment::class.simpleName.plus("low_risk_2"))
    }

    @Screenshot
    @Test
    fun capture_screenshot_increased_risk() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            HomeData.Tracing.INCREASED_RISK_ITEM
        )
        captureScreenshot("increased_risk")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_disabled() {
        every { homeFragmentViewModel.tracingHeaderState } returns MutableLiveData(TracingHeaderState.TracingInActive)
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            HomeData.Tracing.TRACING_DISABLED_ITEM
        )
        captureScreenshot("tracing_disabled")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_progress_downloading() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            HomeData.Tracing.TRACING_PROGRESS_ITEM
        )
        captureScreenshot("progress_downloading")
    }

    @Screenshot
    @Test
    fun capture_screenshot_tracing_failed() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            HomeData.Tracing.TRACING_FAILED_ITEM
        )
        captureScreenshot("tracing_failed")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_submission_done() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_SUBMISSION_DONE_ITEM
        )
        captureScreenshot("submission_done")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_error() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_ERROR_ITEM
        )
        captureScreenshot("test_error")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_fetching() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_FETCHING_ITEM
        )
        captureScreenshot("test_fetching")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_invalid() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_INVALID_ITEM
        )
        captureScreenshot("test_invalid")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_negative() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_NEGATIVE_ITEM
        )
        captureScreenshot("test_negative")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_positive() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_POSITIVE_ITEM
        )
        captureScreenshot("test_positive")
    }

    @Screenshot
    @Test
    fun capture_screenshot_test_pending() {
        every { homeFragmentViewModel.homeItems } returns itemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_PENDING_ITEM
        )
        captureScreenshot("test_pending")
    }

    private fun captureScreenshot(nameSuffix: String) {
        val name = HomeFragment::class.simpleName + "_" + nameSuffix
        launchActivity<MainActivity>()
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
                add(FAQCard.Item {})
            }
        )

    // Helpers
    private fun mainActivityViewModel() = spyk(
        MainActivityViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            environmentSetup = environmentSetup,
            backgroundModeStatus = backgroundModeStatus
        )
    )

    private fun homeFragmentViewModelSpy() = spyk(
        HomeFragmentViewModel(
            dispatcherProvider = TestDispatcherProvider(),
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

    private fun setupHomeFragmentViewModel() {
        every { tracingStatus.generalStatus } returns flowOf()
        every { tracingStateProviderFactory.create(any()) } returns tracingStateProvider.apply {
            every { state } returns flowOf()
        }
        every { submissionStateProvider.state } returns flowOf()
        every { statisticsProvider.current } returns flowOf()
        every { appConfigProvider.currentConfig } returns flowOf()
        homeFragmentViewModel = homeFragmentViewModelSpy()
        with(homeFragmentViewModel) {
            every { observeTestResultToSchedulePositiveTestResultReminder() } just Runs
            every { refreshRequiredData() } just Runs
            every { tracingHeaderState } returns MutableLiveData(TracingHeaderState.TracingActive)
            every { showLoweredRiskLevelDialog } returns MutableLiveData()
            every { homeItems } returns MutableLiveData(emptyList())
            every { popupEvents } returns SingleLiveEvent()
        }

        setupMockViewModel(
            object : HomeFragmentViewModel.Factory {
                override fun create(): HomeFragmentViewModel = homeFragmentViewModel
            }
        )
    }

    private fun setupActivityViewModel() {
        mainActivityViewModel = mainActivityViewModel()
        every { mainActivityViewModel.doBackgroundNoiseCheck() } just Runs
        setupMockViewModel(
            object : MainActivityViewModel.Factory {
                override fun create(): MainActivityViewModel = mainActivityViewModel
            }
        )
    }
}

// MainActivity DI Modules
@Module
abstract class MainActivityTestModule {
    @ContributesAndroidInjector(modules = [MainProviderModule::class])
    abstract fun mainActivity(): MainActivity
}

@Module
class MainProviderModule {
    @Provides
    fun powerManagement(): PowerManagement = mockk(relaxed = true)

    @Provides
    fun deadmanScheduler(): DeadmanNotificationScheduler =
        mockk<DeadmanNotificationScheduler>(relaxed = true).apply {
            every { schedulePeriodic() } just Runs
        }

    @Provides
    fun contactDiaryWorkScheduler(): ContactDiaryWorkScheduler =
        mockk<ContactDiaryWorkScheduler>(relaxed = true).apply {
            every { schedulePeriodic() } just Runs
        }

    @Provides
    fun settings(): ContactDiarySettings = mockk(relaxed = true)
}
