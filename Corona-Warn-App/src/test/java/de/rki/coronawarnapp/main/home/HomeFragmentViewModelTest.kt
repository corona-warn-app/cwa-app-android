package de.rki.coronawarnapp.main.home

import android.content.Context
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.GeneralTracingStatus.Status
import de.rki.coronawarnapp.ui.main.home.HomeFragmentViewModel
import de.rki.coronawarnapp.ui.main.home.SubmissionCardState
import de.rki.coronawarnapp.ui.main.home.SubmissionCardsStateProvider
import de.rki.coronawarnapp.ui.main.home.TracingHeaderState
import de.rki.coronawarnapp.ui.tracing.card.TracingCardState
import de.rki.coronawarnapp.ui.tracing.card.TracingCardStateProvider
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE_TELETAN
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
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
    @MockK lateinit var settingsViewModel: SettingsViewModel
    @MockK lateinit var tracingCardStateProvider: TracingCardStateProvider
    @MockK lateinit var submissionCardsStateProvider: SubmissionCardsStateProvider
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var testResultNotificationService: TestResultNotificationService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { generalTracingStatus.generalStatus } returns flow { emit(Status.TRACING_ACTIVE) }
        every { submissionCardsStateProvider.state } returns flow { emit(mockk<SubmissionCardState>()) }
        every { tracingCardStateProvider.state } returns flow { emit(mockk<TracingCardState>()) }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): HomeFragmentViewModel = HomeFragmentViewModel(
        dispatcherProvider = TestDispatcherProvider,
        errorResetTool = errorResetTool,
        settingsViewModel = settingsViewModel,
        tracingStatus = generalTracingStatus,
        tracingCardStateProvider = tracingCardStateProvider,
        submissionCardsStateProvider = submissionCardsStateProvider,
        tracingRepository = tracingRepository,
        testResultNotificationService = testResultNotificationService
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
    fun `tracing card status is forwarded`() {
        every { tracingCardStateProvider.state } returns flowOf(mockk())
        createInstance().apply {
            this.tracingCardState.observeForTesting { }
            verify { tracingCardStateProvider.state }
        }
    }

    @Test
    fun `submission card state is forwarded`() {
        every { submissionCardsStateProvider.state } returns flowOf(mockk())
        createInstance().apply {
            this.submissionCardState.observeForTesting { }
            verify { submissionCardsStateProvider.state }
        }
    }

    @Test
    fun `positive test result notification is triggered on positive QR code result`() {
        val state = SubmissionCardState(NetworkRequestWrapper.RequestSuccessful(PAIRED_POSITIVE), true)
        every { submissionCardsStateProvider.state } returns flowOf(state)
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
        val state = SubmissionCardState(NetworkRequestWrapper.RequestSuccessful(PAIRED_POSITIVE_TELETAN), true)
        every { submissionCardsStateProvider.state } returns flowOf(state)
        every { testResultNotificationService.schedulePositiveTestResultReminder() } returns Unit

        runBlocking {
            createInstance().apply {
                observeTestResultToSchedulePositiveTestResultReminder()
                verify { testResultNotificationService.schedulePositiveTestResultReminder() }
            }
        }
    }
}
