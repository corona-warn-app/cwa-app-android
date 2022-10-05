package de.rki.coronawarnapp.main.home

import android.content.Context
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.risk.RiskCardDisplayInfo
import de.rki.coronawarnapp.statistics.local.source.LocalStatisticsProvider
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.GeneralTracingStatus.Status
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.states.TracingStateProvider
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents
import de.rki.coronawarnapp.ui.main.home.HomeFragmentViewModel
import de.rki.coronawarnapp.ui.presencetracing.organizer.TraceLocationOrganizerSettings
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.bluetooth.BluetoothSupport
import de.rki.coronawarnapp.util.encryptionmigration.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import testhelpers.extensions.observeForTesting
import testhelpers.preferences.FakeDataStore

@ExtendWith(InstantExecutorExtension::class)
class HomeFragmentViewModelTest : BaseTest() {

    @MockK lateinit var generalTracingStatus: GeneralTracingStatus
    @MockK lateinit var context: Context
    @MockK lateinit var errorResetTool: EncryptionErrorResetTool
    @MockK lateinit var tracingStateProvider: TracingStateProvider
    @MockK lateinit var tracingStateProviderFactory: TracingStateProvider.Factory
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var familyTestRepository: FamilyTestRepository
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var statisticsProvider: StatisticsProvider
    @MockK lateinit var localStatisticsProvider: LocalStatisticsProvider
    @MockK lateinit var appShortcutsHelper: AppShortcutsHelper
    @MockK lateinit var traceLocationOrganizerSettings: TraceLocationOrganizerSettings
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var bluetoothSupport: BluetoothSupport
    @MockK lateinit var localStatisticsConfigStorage: LocalStatisticsConfigStorage
    @MockK lateinit var networkStateProvider: NetworkStateProvider
    @MockK lateinit var recycledTestProvider: RecycledCoronaTestsProvider
    @MockK lateinit var riskCardDisplayInfo: RiskCardDisplayInfo

    private val dataStore = FakeDataStore()
    private val tracingSettings = TracingSettings(dataStore)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { generalTracingStatus.generalStatus } returns flow { emit(Status.TRACING_ACTIVE) }

        every { tracingStateProviderFactory.create(isDetailsMode = false) } returns tracingStateProvider
        every { tracingStateProvider.state } returns flowOf(mockk<LowRisk>())

        every { coronaTestRepository.coronaTests } returns emptyFlow()

        coEvery { appConfigProvider.currentConfig } returns emptyFlow()
        coEvery { statisticsProvider.current } returns emptyFlow()

        coEvery { localStatisticsProvider.current } returns emptyFlow()

        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(100101010)

        bluetoothSupport.apply {
            every { isAdvertisingSupported } returns true
            every { isScanningSupported } returns true
        }

        coEvery { networkStateProvider.networkState } returns emptyFlow()

        every { errorResetTool.isResetNoticeToBeShown } returns false
        coEvery { cwaSettings.wasTracingExplanationDialogShown } returns flowOf(true)

        coEvery { riskCardDisplayInfo.shouldShowRiskCard(any()) } returns true
        every { familyTestRepository.familyTests } returns flowOf()
    }

    @AfterEach
    fun cleanup() {
        dataStore.reset()
    }

    private fun createInstance(): HomeFragmentViewModel = HomeFragmentViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        errorResetTool = errorResetTool,
        tracingStatus = generalTracingStatus,
        tracingRepository = tracingRepository,
        submissionRepository = submissionRepository,
        coronaTestRepository = coronaTestRepository,
        tracingStateProviderFactory = tracingStateProviderFactory,
        cwaSettings = cwaSettings,
        appConfigProvider = appConfigProvider,
        statisticsProvider = statisticsProvider,
        localStatisticsProvider = localStatisticsProvider,
        appShortcutsHelper = appShortcutsHelper,
        tracingSettings = tracingSettings,
        traceLocationOrganizerSettings = traceLocationOrganizerSettings,
        timeStamper = timeStamper,
        bluetoothSupport = bluetoothSupport,
        localStatisticsConfigStorage = localStatisticsConfigStorage,
        networkStateProvider = networkStateProvider,
        recycledTestProvider = recycledTestProvider,
        riskCardDisplayInfo = riskCardDisplayInfo,
        familyTestRepository = familyTestRepository
    )

    @Test
    fun `tracing header status is forwarded`() {
        every { generalTracingStatus.generalStatus } returns flowOf(Status.BLUETOOTH_DISABLED)

        createInstance().apply {
            tracingHeaderState.observeForTesting {
                tracingHeaderState.value shouldBe TracingHeaderState.BluetoothDisabled
            }
        }

        every { generalTracingStatus.generalStatus } returns flowOf(Status.LOCATION_DISABLED)

        createInstance().apply {
            tracingHeaderState.observeForTesting {
                tracingHeaderState.value shouldBe TracingHeaderState.LocationDisabled
            }
        }

        every { generalTracingStatus.generalStatus } returns flowOf(Status.TRACING_INACTIVE)

        createInstance().apply {
            tracingHeaderState.observeForTesting {
                tracingHeaderState.value shouldBe TracingHeaderState.TracingInActive
            }
        }

        every { generalTracingStatus.generalStatus } returns flowOf(Status.TRACING_ACTIVE)

        createInstance().apply {
            tracingHeaderState.observeForTesting {
                tracingHeaderState.value shouldBe TracingHeaderState.TracingActive
            }
        }
    }

    @Test
    fun `simple home item generation`() {
        createInstance().apply {
            this.homeItems.observeForTesting { }
            coVerify {
                tracingStateProvider.state
                coronaTestRepository.coronaTests
            }
        }
    }

    @Test
    fun `test correct order of displaying delta onboarding, release notes and popups`() {
        coEvery { cwaSettings.wasInteroperabilityShownAtLeastOnce } returns flowOf(false) andThen flowOf(true)

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_CODE } returns 1120004
        coEvery { cwaSettings.lastChangelogVersion } returns flowOf(1L) andThen flowOf(1120004)

        every { errorResetTool.isResetNoticeToBeShown } returns true

        with(createInstance()) {
            showPopUps()
            events.getOrAwaitValue() shouldBe HomeFragmentEvents.ShowErrorResetDialog
        }
    }

    @Test
    fun `mark risk level badge as seen`() = runTest {
        createInstance().markRiskBadgeAsSeen()
        tracingSettings.showRiskLevelBadge.first() shouldBe false
    }

    @Test
    fun `flag in tracingSettings should be removed once the user dismisses the additional high risk dialog`() =
        runTest {
            createInstance().userHasAcknowledgedAdditionalHighRiskLevel()
            tracingSettings.isUserToBeNotifiedOfAdditionalHighRiskLevel.first() shouldBe false
        }
}
