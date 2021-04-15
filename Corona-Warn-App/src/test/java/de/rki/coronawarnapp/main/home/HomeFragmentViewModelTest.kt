package de.rki.coronawarnapp.main.home

import android.content.Context
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
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
import de.rki.coronawarnapp.util.encryptionmigration.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import testhelpers.extensions.observeForTesting

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class HomeFragmentViewModelTest : BaseTest() {

    @MockK lateinit var generalTracingStatus: GeneralTracingStatus
    @MockK lateinit var context: Context
    @MockK lateinit var errorResetTool: EncryptionErrorResetTool
    @MockK lateinit var tracingStateProvider: TracingStateProvider
    @MockK lateinit var tracingStateProviderFactory: TracingStateProvider.Factory
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var shareTestResultNotificationService: ShareTestResultNotificationService
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var statisticsProvider: StatisticsProvider
    @MockK lateinit var deadmanNotificationScheduler: DeadmanNotificationScheduler
    @MockK lateinit var appShortcutsHelper: AppShortcutsHelper
    @MockK lateinit var tracingSettings: TracingSettings
    @MockK lateinit var traceLocationOrganizerSettings: TraceLocationOrganizerSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { generalTracingStatus.generalStatus } returns flow { emit(Status.TRACING_ACTIVE) }

        every { tracingStateProviderFactory.create(isDetailsMode = false) } returns tracingStateProvider
        every { tracingStateProvider.state } returns flowOf(mockk<LowRisk>())

        every { coronaTestRepository.coronaTests } returns emptyFlow()

        coEvery { appConfigProvider.currentConfig } returns emptyFlow()
        coEvery { statisticsProvider.current } returns emptyFlow()
    }

    private fun createInstance(): HomeFragmentViewModel = HomeFragmentViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        errorResetTool = errorResetTool,
        tracingStatus = generalTracingStatus,
        tracingRepository = tracingRepository,
        shareTestResultNotificationService = shareTestResultNotificationService,
        submissionRepository = submissionRepository,
        coronaTestRepository = coronaTestRepository,
        tracingStateProviderFactory = tracingStateProviderFactory,
        cwaSettings = cwaSettings,
        appConfigProvider = appConfigProvider,
        statisticsProvider = statisticsProvider,
        deadmanNotificationScheduler = deadmanNotificationScheduler,
        appShortcutsHelper = appShortcutsHelper,
        tracingSettings = tracingSettings,
        traceLocationOrganizerSettings = traceLocationOrganizerSettings
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
    fun `positive test result notification is triggered on positive QR code result`() {
//        every { submissionRepository.deviceUIStateFlow } returns flowOf(
//            NetworkRequestWrapper.RequestSuccessful(PAIRED_POSITIVE)
//        )
        every { submissionRepository.pcrTest } returns flowOf(
            mockk<PCRCoronaTest>().apply { every { testResult } returns CoronaTestResult.PCR_POSITIVE }
        )
        every { shareTestResultNotificationService.scheduleSharePositiveTestResultReminder() } returns Unit

        runBlocking {
            createInstance().apply {
                observeTestResultToSchedulePositiveTestResultReminder()
                verify { shareTestResultNotificationService.scheduleSharePositiveTestResultReminder() }
            }
        }
    }

    @Test
    fun `positive test result notification is triggered on positive TeleTan code result`() {
//        every { submissionRepository.deviceUIStateFlow } returns flowOf(
//            NetworkRequestWrapper.RequestSuccessful(PAIRED_POSITIVE_TELETAN)
//        )
        every { submissionRepository.pcrTest } returns flowOf(
            mockk<PCRCoronaTest>().apply { every { testResult } returns CoronaTestResult.PCR_POSITIVE }
        )
        every { shareTestResultNotificationService.scheduleSharePositiveTestResultReminder() } returns Unit

        runBlocking {
            createInstance().apply {
                observeTestResultToSchedulePositiveTestResultReminder()
                verify { shareTestResultNotificationService.scheduleSharePositiveTestResultReminder() }
            }
        }
    }

    @Test
    fun `test correct order of displaying delta onboarding, release notes and popups`() {
        every { cwaSettings.wasInteroperabilityShownAtLeastOnce } returns false andThen true

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_CODE } returns 1120004
        every { cwaSettings.lastChangelogVersion.value } returns 1L andThen 1120004

        every { errorResetTool.isResetNoticeToBeShown } returns true

        with(createInstance()) {
            showPopUps()
            popupEvents.getOrAwaitValue() shouldBe HomeFragmentEvents.ShowErrorResetDialog
        }
    }
}
