package de.rki.coronawarnapp.main.home

import android.content.Context
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionDone
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionStateProvider
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.GeneralTracingStatus.Status
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.states.TracingStateProvider
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState
import de.rki.coronawarnapp.ui.main.home.HomeFragmentViewModel
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE_TELETAN
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.observeForTesting

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class HomeFragmentViewModelTest : BaseTest() {

    @MockK lateinit var generalTracingStatus: GeneralTracingStatus
    @MockK lateinit var context: Context
    @MockK lateinit var errorResetTool: EncryptionErrorResetTool
    @MockK lateinit var tracingStateProvider: TracingStateProvider
    @MockK lateinit var tracingStateProviderFactory: TracingStateProvider.Factory
    @MockK lateinit var submissionStateProvider: SubmissionStateProvider
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var testResultNotificationService: TestResultNotificationService
    @MockK lateinit var submissionRepository: SubmissionRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { generalTracingStatus.generalStatus } returns flow { emit(Status.TRACING_ACTIVE) }

        every { tracingStateProviderFactory.create(isDetailsMode = false) } returns tracingStateProvider
        every { tracingStateProvider.state } returns flowOf(mockk<LowRisk>())

        every { submissionStateProvider.state } returns flowOf(SubmissionDone)

        every { submissionRepository.hasViewedTestResult } returns flowOf(true)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): HomeFragmentViewModel = HomeFragmentViewModel(
        dispatcherProvider = TestDispatcherProvider,
        errorResetTool = errorResetTool,
        tracingStatus = generalTracingStatus,
        tracingRepository = tracingRepository,
        testResultNotificationService = testResultNotificationService,
        submissionRepository = submissionRepository,
        submissionStateProvider = submissionStateProvider,
        tracingStateProviderFactory = tracingStateProviderFactory
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
                submissionStateProvider.state
            }
        }
    }

    @Test
    fun `positive test result notification is triggered on positive QR code result`() {
        every { submissionRepository.deviceUIStateFlow } returns flowOf(
            NetworkRequestWrapper.RequestSuccessful(PAIRED_POSITIVE)
        )
        every { testResultNotificationService.schedulePositiveTestResultReminder() } returns Unit

        runBlocking {
            createInstance().apply {
                observeTestResultToSchedulePositiveTestResultReminder()
                verify { testResultNotificationService.schedulePositiveTestResultReminder() }
            }
        }
    }

    @Test
    fun `positive test result notification is triggered on positive TeleTan code result`() {
        every { submissionRepository.deviceUIStateFlow } returns flowOf(
            NetworkRequestWrapper.RequestSuccessful(PAIRED_POSITIVE_TELETAN)
        )
        every { testResultNotificationService.schedulePositiveTestResultReminder() } returns Unit

        runBlocking {
            createInstance().apply {
                observeTestResultToSchedulePositiveTestResultReminder()
                verify { testResultNotificationService.schedulePositiveTestResultReminder() }
            }
        }
    }
}
