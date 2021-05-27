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
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsHomeCard
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.TestResultItem
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.states.TracingStateProvider
import de.rki.coronawarnapp.tracing.ui.homecards.TracingStateItem
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.TraceLocationOrganizerSettings
import de.rki.coronawarnapp.ui.statistics.Statistics
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.bluetooth.BluetoothSupport
import de.rki.coronawarnapp.util.encryptionmigration.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.vaccination.core.VaccinationSettings
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.homecard.CreateVaccinationHomeCard
import de.rki.coronawarnapp.vaccination.ui.homecard.VaccinationStatusItem
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
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragment2
import testhelpers.launchInMainActivity
import testhelpers.recyclerScrollTo
import testhelpers.takeScreenshot
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest : BaseUITest() {

    @MockK lateinit var errorResetTool: EncryptionErrorResetTool
    @MockK lateinit var tracingStatus: GeneralTracingStatus
    @MockK lateinit var tracingStateProviderFactory: TracingStateProvider.Factory
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var statisticsProvider: StatisticsProvider
    @MockK lateinit var appShortcutsHelper: AppShortcutsHelper
    @MockK lateinit var tracingSettings: TracingSettings
    @MockK lateinit var vaccinationSettings: VaccinationSettings
    @MockK lateinit var traceLocationOrganizerSettings: TraceLocationOrganizerSettings
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var bluetoothSupport: BluetoothSupport
    @MockK lateinit var vaccinationRepository: VaccinationRepository

    private lateinit var homeFragmentViewModel: HomeFragmentViewModel

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        homeFragmentViewModel = homeFragmentViewModelSpy()
        with(homeFragmentViewModel) {
            every { refreshRequiredData() } just Runs
            every { tracingHeaderState } returns MutableLiveData(TracingHeaderState.TracingActive)
            every { showLoweredRiskLevelDialog } returns MutableLiveData()
            every { homeItems } returns homeFragmentItemsLiveData()
            every { events } returns SingleLiveEvent()
            every { showPopUps() } just Runs
            every { restoreAppShortcuts() } just Runs
        }

        setupMockViewModel(
            object : HomeFragmentViewModel.Factory {
                override fun create(): HomeFragmentViewModel = homeFragmentViewModel
            }
        )

        every { bluetoothSupport.isScanningSupported } returns true
        every { bluetoothSupport.isAdvertisingSupported } returns true
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
    fun captureHomeFragmentLowRiskNoEncountersWithoutInstallTime() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            HomeData.Tracing.LOW_RISK_ITEM_NO_ENCOUNTERS_WITHOUT_INSTALL_TIME
        )
        captureHomeFragment("low_risk_no_encounters_without_install_time")
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
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(3))
        Statistics.statisticsData?.items?.forEachIndexed { index, _ ->
            onView(withId(R.id.statistics_recyclerview)).perform(recyclerScrollTo(index))
            takeScreenshot<HomeFragment>("statistics_card_$index")
        }
    }

    @Screenshot
    @Test
    fun captureHomeFragmentCompatibilityBleBroadcastNotSupported() {
        every { homeFragmentViewModel.homeItems } returns
            homeFragmentItemsLiveData(HomeData.Tracing.TRACING_FAILED_ITEM)
        every { bluetoothSupport.isScanningSupported } returns true
        every { bluetoothSupport.isAdvertisingSupported } returns false
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(2))
        captureHomeFragment("compatibility_ble_broadcast_not_supported")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentCompatibilityBleScanNotSupported() {
        every { homeFragmentViewModel.homeItems } returns
            homeFragmentItemsLiveData(HomeData.Tracing.TRACING_FAILED_ITEM)
        every { bluetoothSupport.isScanningSupported } returns false
        every { bluetoothSupport.isAdvertisingSupported } returns true
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(2))
        captureHomeFragment("compatibility_ble_scan_not_supported")
    }

    @Screenshot
    @Test
    fun captureVaccinationNoCertificate() {
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(2, additionalY = 450))

        takeScreenshot<HomeFragment>("vaccination_none")
    }

    @Screenshot
    @Test
    fun captureVaccinationIncomplete() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            vaccinationStatus = HomeData.Vaccination.INCOMPLETE
        )
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(2))

        takeScreenshot<HomeFragment>("vaccination_incomplete")
    }

    @Screenshot
    @Test
    fun captureVaccinationComplete() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            vaccinationStatus = HomeData.Vaccination.COMPLETE
        )
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(2))

        takeScreenshot<HomeFragment>("vaccination_complete")
    }

    @Screenshot
    @Test
    fun captureVaccinationImmunity() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            vaccinationStatus = HomeData.Vaccination.IMMUNITY
        )
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(2))

        takeScreenshot<HomeFragment>("vaccination_immunity")
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun onResumeCallsRefresh() {
        launchFragment2<HomeFragment>()
        verify(exactly = 1) { homeFragmentViewModel.refreshRequiredData() }
    }

    private fun captureHomeFragment(nameSuffix: String) {
        launchInMainActivity<HomeFragment>()
        takeScreenshot<HomeFragment>(nameSuffix)
    }

    private fun homeFragmentViewModelSpy() = spyk(
        HomeFragmentViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            errorResetTool = errorResetTool,
            tracingRepository = tracingRepository,
            tracingStateProviderFactory = tracingStateProviderFactory,
            appConfigProvider = appConfigProvider,
            tracingStatus = tracingStatus,
            submissionRepository = submissionRepository,
            coronaTestRepository = coronaTestRepository,
            cwaSettings = cwaSettings,
            statisticsProvider = statisticsProvider,
            appShortcutsHelper = appShortcutsHelper,
            tracingSettings = tracingSettings,
            traceLocationOrganizerSettings = traceLocationOrganizerSettings,
            timeStamper = timeStamper,
            bluetoothSupport = bluetoothSupport,
            vaccinationSettings = vaccinationSettings,
            vaccinationRepository = vaccinationRepository
        )
    )

    // LiveData item for fragments
    private fun homeFragmentItemsLiveData(
        tracingStateItem: TracingStateItem = HomeData.Tracing.LOW_RISK_ITEM_WITH_ENCOUNTERS,
        submissionTestResultItem: TestResultItem = HomeData.Submission.TEST_UNREGISTERED_ITEM,
        vaccinationStatus: VaccinationStatusItem? = null,
    ): LiveData<List<HomeItem>> =
        MutableLiveData(
            mutableListOf<HomeItem>().apply {
                when (submissionTestResultItem) {
                    is PcrTestSubmissionDoneCard.Item,
                    is PcrTestPositiveCard.Item -> {
                        Timber.d("Tracing item is not added, submission:$submissionTestResultItem")
                    }
                    else -> add(tracingStateItem)
                }

                vaccinationStatus?.let {
                    add(it)
                }

                add(submissionTestResultItem)

                add(CreateVaccinationHomeCard.Item {})

                Statistics.statisticsData?.let {
                    add(StatisticsHomeCard.Item(data = it, onHelpAction = { }))
                }
                add(FAQCard.Item {})
            }
        )
}

@Module
abstract class HomeFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun homeScreen(): HomeFragment
}
