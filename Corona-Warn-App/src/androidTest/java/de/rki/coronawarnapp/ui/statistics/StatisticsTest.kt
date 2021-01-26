package de.rki.coronawarnapp.ui.statistics

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.environment.download.DownloadCDNModule
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.statistics.StatisticsModule
import de.rki.coronawarnapp.statistics.source.StatisticsParser
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.statistics.source.StatisticsServer
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsHomeCard
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
import de.rki.coronawarnapp.ui.main.home.items.DiaryCard
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.security.VerificationKeys
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.converter.gson.GsonConverterFactory
import testhelpers.BaseUITest
import testhelpers.SCREENSHOT_DELAY_TIME
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.TestDispatcherProvider
import testhelpers.launchFragmentInContainer2
import testhelpers.recyclerScrollTo
import timber.log.Timber
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class StatisticsTest : BaseUITest() {

    @MockK lateinit var errorResetTool: EncryptionErrorResetTool
    @MockK lateinit var tracingStatus: GeneralTracingStatus
    @MockK lateinit var tracingStateProviderFactory: TracingStateProvider.Factory
    @MockK lateinit var submissionStateProvider: SubmissionStateProvider
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var shareTestResultNotificationService: ShareTestResultNotificationService
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var cache: Cache
    @MockK lateinit var context: Context
    @MockK lateinit var preferences: SharedPreferences
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

        if (statisticsData == null) {
            statisticsData = loadRealStatisticsData()
        }

        viewModel = homeFragmentViewModelSpy()
        with(viewModel) {
            every { observeTestResultToSchedulePositiveTestResultReminder() } just Runs
            every { refreshRequiredData() } just Runs
            every { tracingHeaderState } returns MutableLiveData(TracingHeaderState.TracingActive)
            every { showLoweredRiskLevelDialog } returns MutableLiveData()
            every { popupEvents } returns SingleLiveEvent()
            every { homeItems } returns itemsLiveData(HomeData.Tracing.LOW_RISK_ITEM, statsData = statisticsData)
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

    @Screenshot
    @Test
    fun capture_screenshot_low_risk() {
        launchFragmentInContainer2<HomeFragment>()
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        onView(withId(R.id.recycler_view)).perform(recyclerScrollTo(3))

        for (i in 0 until (statisticsData?.items?.size ?: 0)) {
            onView(withId(R.id.statistics_recyclerview)).perform(recyclerScrollTo(i))
            Thread.sleep(SCREENSHOT_DELAY_TIME)
            Screengrab.screenshot(HomeFragment::class.simpleName.plus("_statistics_card_$i"))
        }
    }

    private fun itemsLiveData(
        tracingStateItem: TracingStateItem = HomeData.Tracing.LOW_RISK_ITEM,
        submissionTestResultItem: TestResultItem = HomeData.Submission.TEST_UNREGISTERED_ITEM,
        statsData: StatisticsData? = null
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

                if (statsData != null) {
                    add(StatisticsHomeCard.Item(data = statsData, onHelpAction = { }))
                }

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

    private fun loadRealStatisticsData(): StatisticsData? {
        every { preferences.getString(any(), any()) } returns null
        every { context.getSharedPreferences(any(), any()) } returns preferences

        val cdnModule = DownloadCDNModule()
        val baseGson = SerializationModule().baseGson()
        val environmentSetup = EnvironmentSetup(context = context, gson = baseGson)
        val httpClient = HttpModule().defaultHttpClient()
        val cdnClient = cdnModule.cdnHttpClient(httpClient)
        val url = cdnModule.provideDownloadServerUrl(environmentSetup)
        val verificationKeys = VerificationKeys(environmentSetup)
        val gsonFactory = GsonConverterFactory.create()

        val statisticsServer = StatisticsServer(
            api = {
                StatisticsModule().api(
                    client = cdnClient,
                    url = url,
                    gsonConverterFactory = gsonFactory,
                    cache = cache
                )
            },
            cache = cache,
            verificationKeys = verificationKeys,
        )

        return runBlocking {
            try {
                val rawData = statisticsServer.getRawStatistics()
                StatisticsParser().parse(rawData)
            } catch (e: Exception) {
                Timber.e(e, "Can't download statistics data. Check your internet connection.")
                null
            }
        }
    }

    companion object {
        private var statisticsData: StatisticsData? = null
    }
}
