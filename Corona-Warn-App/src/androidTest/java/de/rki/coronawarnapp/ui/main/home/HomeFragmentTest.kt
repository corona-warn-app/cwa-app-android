package de.rki.coronawarnapp.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsHomeCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.TestResultItem
import de.rki.coronawarnapp.tracing.ui.homecards.TracingStateItem
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.ui.statistics.Statistics
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragment2
import testhelpers.launchInMainActivity
import testhelpers.recyclerScrollTo
import testhelpers.setViewVisibility
import testhelpers.takeScreenshot
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest : BaseUITest() {

    @MockK lateinit var homeFragmentViewModel: HomeFragmentViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        with(homeFragmentViewModel) {
            every { refreshRequiredData() } just Runs
            every { tracingHeaderState } returns MutableLiveData(TracingHeaderState.TracingActive)
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
    fun captureHomeFragmentLowRiskNoEncounters_Tooltip() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            HomeData.Tracing.LOW_RISK_ITEM_NO_ENCOUNTERS
        )
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.fake_fab_tooltip)).perform(setViewVisibility(true))
        takeScreenshot<HomeFragment>("fab_tooltip")
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
            submissionTestResultItems = listOf(HomeData.Submission.TEST_SUBMISSION_DONE_ITEM)
        )
        captureHomeFragment("submission_done")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestError() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItems = listOf(HomeData.Submission.TEST_ERROR_ITEM)
        )
        captureHomeFragment("test_error")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestFetching() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItems = listOf(HomeData.Submission.TEST_FETCHING_ITEM)
        )
        captureHomeFragment("test_fetching")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestInvalid() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItems = listOf(HomeData.Submission.TEST_INVALID_ITEM)
        )
        captureHomeFragment("test_invalid")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestNegative() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItems = listOf(HomeData.Submission.TEST_NEGATIVE_ITEM)
        )
        captureHomeFragment("test_negative")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTwoTestsNegative() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItems = listOf(
                HomeData.Submission.TEST_NEGATIVE_ITEM,
                HomeData.Submission.TEST_NEGATIVE_ITEM_RAT
            )
        )
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(2, additionalY = -25))
        takeScreenshot<HomeFragment>("tests_negative")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestPositive() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItems = listOf(HomeData.Submission.TEST_POSITIVE_ITEM)
        )
        captureHomeFragment("test_positive")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentTestPending() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(
            submissionTestResultItems = listOf(HomeData.Submission.TEST_PENDING_ITEM)
        )
        captureHomeFragment("test_pending")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentStatistics() {
        every { homeFragmentViewModel.homeItems } returns homeFragmentItemsLiveData(HomeData.Tracing.LOW_RISK_ITEM_WITH_ENCOUNTERS)
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(3))
        Statistics.statisticsData.items.forEachIndexed { index, _ ->
            onView(withId(R.id.statistics_recyclerview)).perform(
                recyclerScrollTo(
                    position = index,
                    additionalX = if (index > 0) 100 else 0
                )
            )
            takeScreenshot<HomeFragment>("statistics_card_$index")
        }
    }

    @Screenshot
    @Test
    fun captureHomeFragmentCompatibilityBleBroadcastNotSupported() {
        every { homeFragmentViewModel.homeItems } returns
            homeFragmentItemsLiveData(HomeData.Tracing.TRACING_FAILED_ITEM)
        launchInMainActivity<HomeFragment>()
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(2))
        captureHomeFragment("compatibility_ble_broadcast_not_supported")
    }

    @Screenshot
    @Test
    fun captureHomeFragmentCompatibilityBleScanNotSupported() {
        every { homeFragmentViewModel.homeItems } returns
            homeFragmentItemsLiveData(HomeData.Tracing.TRACING_FAILED_ITEM)
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

    // LiveData item for fragments
    private fun homeFragmentItemsLiveData(
        tracingStateItem: TracingStateItem = HomeData.Tracing.LOW_RISK_ITEM_WITH_ENCOUNTERS,
        submissionTestResultItems: List<TestResultItem> = listOf(HomeData.Submission.TEST_UNREGISTERED_ITEM)
    ): LiveData<List<HomeItem>> =
        MutableLiveData(
            mutableListOf<HomeItem>().apply {

                val hideTracingState = submissionTestResultItems.any {
                    it is PcrTestPositiveCard.Item ||
                        it is PcrTestSubmissionDoneCard.Item ||
                        it is RapidTestPositiveCard.Item ||
                        it is RapidTestSubmissionDoneCard.Item
                }

                if (hideTracingState) {
                    Timber.d("Tracing item is not added, submission:$submissionTestResultItems")
                } else {
                    add(tracingStateItem)
                }

                addAll(submissionTestResultItems)
                add(StatisticsHomeCard.Item(data = Statistics.statisticsData, onClickListener = { }))
                add(FAQCard.Item {})
            }
        )
}

@Module
abstract class HomeFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun homeScreen(): HomeFragment
}
