package de.rki.coronawarnapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryWorkScheduler
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.ContactDiarySettings
import de.rki.coronawarnapp.contactdiary.ui.exporter.ContactDiaryExporter
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragment
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewViewModel
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.subheader.OverviewSubHeaderItem
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsScheduler
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsHomeCard
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionStateProvider
import de.rki.coronawarnapp.submission.ui.homecards.TestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.TestResultItem
import de.rki.coronawarnapp.submission.ui.homecards.TestSubmissionDoneCard
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.states.TracingStateProvider
import de.rki.coronawarnapp.tracing.ui.homecards.TracingStateItem
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState
import de.rki.coronawarnapp.ui.contactdiary.DiaryData
import de.rki.coronawarnapp.ui.main.home.HomeData
import de.rki.coronawarnapp.ui.main.home.HomeFragment
import de.rki.coronawarnapp.ui.main.home.HomeFragmentViewModel
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.ui.statistics.Statistics
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.device.PowerManagement
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
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
import kotlinx.coroutines.flow.flowOf
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.recyclerScrollTo
import testhelpers.selectBottomNavTab
import testhelpers.takeScreenshot
import timber.log.Timber
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class MainActivityTest : BaseUITest() {
    // HomeFragment mocks
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
    @MockK lateinit var deadmanNotificationScheduler: DeadmanNotificationScheduler
    @MockK lateinit var appShortcutsHelper: AppShortcutsHelper

    // MainActivity mocks
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var tracingStateProvider: TracingStateProvider
    @MockK lateinit var diarySettings: ContactDiarySettings

    // ContactDiaryOverviewFragment mocks
    @MockK lateinit var taskController: TaskController
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var exporter: ContactDiaryExporter

    // ViewModels
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var homeFragmentViewModel: HomeFragmentViewModel
    private lateinit var contactDiaryOverviewViewModel: ContactDiaryOverviewViewModel

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
        every { LocalData.isAllowedToSubmitDiagnosisKeys() } returns false
        every { BackgroundWorkScheduler.startWorkScheduler() } just Runs
        // Setup ViewModels
        setupActivityViewModel()
        setupHomeFragmentViewModel()
        setupContactDiaryOverviewViewModel()
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launchMainActivity() {
        launchActivity<MainActivity>()
    }

    @Screenshot
    @Test
    fun captureHomeFragmentLowRiskNoEncounters() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            HomeData.Tracing.LOW_RISK_ITEM_NO_ENCOUNTERS
        )
        captureHomeFragment("low_risk_no_encounters")

        // also scroll down and capture a screenshot of the faq card
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo())
        takeScreenshot<HomeFragment>("faq_card")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentLowRiskWithEncounters() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            HomeData.Tracing.LOW_RISK_ITEM_WITH_ENCOUNTERS
        )
        captureHomeFragment("low_risk_with_encounters")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentIncreasedRisk() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            HomeData.Tracing.INCREASED_RISK_ITEM
        )
        captureHomeFragment("increased_risk")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTracingDisabled() {
        every { homeFragmentViewModel.tracingHeaderState } returns MutableLiveData(TracingHeaderState.TracingInActive)
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            HomeData.Tracing.TRACING_DISABLED_ITEM
        )
        captureHomeFragment("tracing_disabled")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTracingProgressDownloading() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            HomeData.Tracing.TRACING_PROGRESS_ITEM
        )
        captureHomeFragment("progress_downloading")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTracingFailed() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            HomeData.Tracing.TRACING_FAILED_ITEM
        )
        captureHomeFragment("tracing_failed")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestSubmissionDone() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_SUBMISSION_DONE_ITEM
        )
        captureHomeFragment("submission_done")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestError() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_ERROR_ITEM
        )
        captureHomeFragment("test_error")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestFetching() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_FETCHING_ITEM
        )
        captureHomeFragment("test_fetching")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestInvalid() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_INVALID_ITEM
        )
        captureHomeFragment("test_invalid")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestNegative() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_NEGATIVE_ITEM
        )
        captureHomeFragment("test_negative")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestPositive() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_POSITIVE_ITEM
        )
        captureHomeFragment("test_positive")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestPending() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItem = HomeData.Submission.TEST_PENDING_ITEM
        )
        captureHomeFragment("test_pending")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentStatistics() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(HomeData.Tracing.LOW_RISK_ITEM_WITH_ENCOUNTERS)
        launchActivity<MainActivity>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(3))
        Statistics.statisticsData?.items?.forEachIndexed { index, _ ->
            onView(withId(R.id.statistics_recyclerview)).perform(recyclerScrollTo(index))
            takeScreenshot<HomeFragment>("statistics_card_$index")
        }
    }

    @Screenshot
    @Test
    fun captureContactDiaryOverviewFragment() {
        every { contactDiaryOverviewViewModel.listItems } returns contactDiaryOverviewItemLiveData()
        launchActivity<MainActivity>()
        onView(withId(R.id.main_bottom_navigation))
            .perform(selectBottomNavTab(R.id.contact_diary_nav_graph))
        takeScreenshot<ContactDiaryOverviewFragment>()

        onView(withId(R.id.contact_diary_overview_recyclerview))
            .perform(recyclerScrollTo(4))
        takeScreenshot<ContactDiaryOverviewFragment>("2")
    }

    private fun captureHomeFragment(nameSuffix: String) {
        launchActivity<MainActivity>()
        takeScreenshot<HomeFragment>(nameSuffix)
    }

    // LiveData item for fragments
    private fun homeFragmentItemsLiveData(
        tracingStateItem: TracingStateItem = HomeData.Tracing.LOW_RISK_ITEM_WITH_ENCOUNTERS,
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
                Statistics.statisticsData?.let {
                    add(StatisticsHomeCard.Item(data = it, onHelpAction = { }))
                }
                add(FAQCard.Item {})
            }
        )

    private fun contactDiaryOverviewItemLiveData(): LiveData<List<DiaryOverviewItem>> {
        val data = mutableListOf<DiaryOverviewItem>()
        data.add(OverviewSubHeaderItem)
        val dayData = (0 until ContactDiaryOverviewViewModel.DAY_COUNT)
            .map { LocalDate.now().minusDays(it) }
            .mapIndexed { index, localDate ->
                val dayData = mutableListOf<DayOverviewItem.Data>().apply {
                    if (index == 1) {
                        add(DiaryData.DATA_ITEMS[0])
                        add(DiaryData.DATA_ITEMS[1])
                    } else if (index == 3) {
                        add(DiaryData.DATA_ITEMS[2])
                    }
                }
                val risk = when (index % 5) {
                    3 -> DiaryData.HIGH_RISK_DUE_LOW_RISK_ENCOUNTERS
                    else -> null // DiaryData.LOW_RISK OR DiaryData.HIGH_RISK POSSIBLE
                }
                DayOverviewItem(
                    date = localDate,
                    data = dayData,
                    risk = risk
                ) {
                    // onClick
                }
            }
        data.addAll(dayData)
        return MutableLiveData(data)
    }

    // ViewModels creators
    private fun mainActivityViewModelSpy() = spyk(
        MainActivityViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            environmentSetup = environmentSetup,
            backgroundModeStatus = backgroundModeStatus,
            contactDiarySettings = diarySettings
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
            statisticsProvider = statisticsProvider,
            deadmanNotificationScheduler = deadmanNotificationScheduler,
            appShortcutsHelper = appShortcutsHelper
        )
    )

    private fun contactDiaryOverviewViewModelSpy() = spyk(
        ContactDiaryOverviewViewModel(
            taskController = taskController,
            dispatcherProvider = TestDispatcherProvider(),
            contactDiaryRepository = contactDiaryRepository,
            riskLevelStorage = riskLevelStorage,
            timeStamper = TimeStamper(),
            exporter = exporter
        )
    )

    // Setup ViewModels
    private fun setupContactDiaryOverviewViewModel() {
        every { contactDiaryRepository.locationVisits } returns flowOf()
        every { contactDiaryRepository.personEncounters } returns flowOf()
        every { riskLevelStorage.aggregatedRiskPerDateResults } returns flowOf()
        every { taskController.submit(any()) } just Runs

        contactDiaryOverviewViewModel = contactDiaryOverviewViewModelSpy()
        setupMockViewModel(
            object : ContactDiaryOverviewViewModel.Factory {
                override fun create(): ContactDiaryOverviewViewModel = contactDiaryOverviewViewModel
            }
        )
    }

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
            every { showPopUps() } just Runs
            every { restoreAppShortcuts() } just Runs
        }

        setupMockViewModel(
            object : HomeFragmentViewModel.Factory {
                override fun create(): HomeFragmentViewModel = homeFragmentViewModel
            }
        )
    }

    private fun setupActivityViewModel() {
        every { diarySettings.onboardingStatus } returns ContactDiarySettings.OnboardingStatus.RISK_STATUS_1_12
        mainActivityViewModel = mainActivityViewModelSpy()
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
    fun dataDonationAnalyticsScheduler(): DataDonationAnalyticsScheduler =
        mockk<DataDonationAnalyticsScheduler>(relaxed = true).apply {
            every { schedulePeriodic() } just Runs
        }
}
