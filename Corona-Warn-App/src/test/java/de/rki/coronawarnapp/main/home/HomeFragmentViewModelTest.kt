package de.rki.coronawarnapp.main.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.GeneralTracingStatus.Status
import de.rki.coronawarnapp.ui.main.home.HomeFragmentViewModel
import de.rki.coronawarnapp.ui.main.home.SubmissionCardsViewModel
import de.rki.coronawarnapp.ui.main.home.TracingHeaderState
import de.rki.coronawarnapp.ui.tracing.card.TracingCardState
import de.rki.coronawarnapp.ui.tracing.card.TracingCardViewModel
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
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

    @MockK lateinit var tracing: GeneralTracingStatus
    @MockK lateinit var context: Context
    @MockK lateinit var errorResetTool: EncryptionErrorResetTool
    @MockK lateinit var settingsViewModel: SettingsViewModel
    @MockK lateinit var submissionViewModel: SubmissionViewModel
    @MockK lateinit var tracingCardViewModel: TracingCardViewModel
    @MockK lateinit var submissionCardsViewModel: SubmissionCardsViewModel
    @MockK lateinit var tracingRepository: TracingRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): HomeFragmentViewModel = HomeFragmentViewModel(
        dispatcherProvider = TestDispatcherProvider,
        errorResetTool = errorResetTool,
        settingsViewModel = settingsViewModel,
        tracingStatus = tracing,
        tracingCardViewModel = tracingCardViewModel,
        submissionCardsViewModel = submissionCardsViewModel,
        tracingRepository = tracingRepository
    )

    @Test
    fun `tracing header status is forwarded`() {
        every { tracing.generalStatus } returns flowOf(Status.BLUETOOTH_DISABLED)

        createInstance().apply {
            tracingHeaderState.observeForTesting {
                tracingHeaderState.value shouldBe TracingHeaderState.BluetoothDisabled
            }
        }

        every { tracing.generalStatus } returns flowOf(Status.LOCATION_DISABLED)

        createInstance().apply {
            tracingHeaderState.observeForTesting {
                tracingHeaderState.value shouldBe TracingHeaderState.LocationDisabled
            }
        }

        every { tracing.generalStatus } returns flowOf(Status.TRACING_INACTIVE)

        createInstance().apply {
            tracingHeaderState.observeForTesting {
                tracingHeaderState.value shouldBe TracingHeaderState.TracingInActive
            }
        }

        every { tracing.generalStatus } returns flowOf(Status.TRACING_ACTIVE)

        createInstance().apply {
            tracingHeaderState.observeForTesting {
                tracingHeaderState.value shouldBe TracingHeaderState.TracingActive
            }
        }
    }

    @Test
    fun `tracing card status is forwarded`() {
        every { tracingCardViewModel.state } returns MutableLiveData<TracingCardState>(mockk())
        createInstance().apply {
            this.tracingCardState.observeForTesting { }
            verify { tracingCardViewModel.state }
        }
    }

    @Test
    fun `submission card state is forwarded`() {
        TODO()
    }
}
