package de.rki.coronawarnapp.test.risklevel.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.ui.tracing.card.TracingCardStateProvider
import de.rki.coronawarnapp.util.di.AppInjector
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
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

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(RetrieveDiagnosisKeysTransaction)
        coEvery { RetrieveDiagnosisKeysTransaction.start() } returns Unit

        coEvery { keyCacheRepository.clear() } returns Unit
        every { enfClient.internalClient } returns exposureNotificationClient
        every { tracingCardStateProvider.state } returns flowOf(mockk())
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
            dispatcherProvider = TestDispatcherProvider
        )

    @Test
    fun `action retrieveDiagnosisKeys, retieves diagnosis keys and calls risklevel calculation`() {
        val vm = createViewModel()

        vm.retrieveDiagnosisKeys()

        coVerifyOrder {
            RetrieveDiagnosisKeysTransaction.start()
            vm.calculateRiskLevel()
        }
    }

    @Test
    fun `action calculateRiskLevel, calls risklevel calculation`() {
        val vm = createViewModel()

        vm.calculateRiskLevel()

        coVerify(exactly = 1) { AppInjector.component.taskController.submit(
            DefaultTaskRequest(
                RiskLevelTask::class)
        ) }
        coVerify(exactly = 0) { RetrieveDiagnosisKeysTransaction.start() }
    }

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
