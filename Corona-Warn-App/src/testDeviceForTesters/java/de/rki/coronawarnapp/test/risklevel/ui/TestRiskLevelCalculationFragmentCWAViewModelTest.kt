package de.rki.coronawarnapp.test.risklevel.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.risk.RiskLevels
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.ui.tracing.card.TracingCardStateProvider
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class TestRiskLevelCalculationFragmentCWAViewModelTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var savedStateHandle: SavedStateHandle
    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var exposureNotificationClient: ExposureNotificationClient
    @MockK lateinit var keyCacheRepository: KeyCacheRepository
    @MockK lateinit var tracingCardStateProvider: TracingCardStateProvider
    @MockK lateinit var taskController: TaskController
    @MockK lateinit var riskLevels: RiskLevels

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { keyCacheRepository.clear() } returns Unit
        every { enfClient.internalClient } returns exposureNotificationClient
        every { tracingCardStateProvider.state } returns flowOf(mockk())
        every { taskController.submit(any()) } just Runs
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createViewModel(exampleArgs: String? = null): TestRiskLevelCalculationFragmentCWAViewModel =
        TestRiskLevelCalculationFragmentCWAViewModel(
            handle = savedStateHandle,
            exampleArg = exampleArgs,
            context = context,
            enfClient = enfClient,
            keyCacheRepository = keyCacheRepository,
            tracingCardStateProvider = tracingCardStateProvider,
            dispatcherProvider = TestDispatcherProvider,
            riskLevels = riskLevels,
            taskController = taskController
        )

    @Test
    fun `action clearDiagnosisKeys calls the keyCacheRepo`() {
        val vm = createViewModel()

        vm.clearKeyCache()

        coVerify(exactly = 1) { keyCacheRepository.clear() }
    }

    @Test
    fun `action scanLocalQRCodeAndProvide, triggers event`() {
        val vm = createViewModel()

        vm.startLocalQRCodeScanEvent.value shouldBe null

        vm.scanLocalQRCodeAndProvide()

        vm.startLocalQRCodeScanEvent.value shouldBe Unit
    }
}
