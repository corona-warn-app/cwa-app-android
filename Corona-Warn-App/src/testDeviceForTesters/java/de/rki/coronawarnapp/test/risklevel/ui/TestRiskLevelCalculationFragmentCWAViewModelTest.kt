package de.rki.coronawarnapp.test.risklevel.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class TestRiskLevelCalculationFragmentCWAViewModelTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var savedStateHandle: SavedStateHandle
    @MockK lateinit var exposureNotificationClient: ExposureNotificationClient
    @MockK lateinit var keyCacheRepository: KeyCacheRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(RetrieveDiagnosisKeysTransaction)
        coEvery { RetrieveDiagnosisKeysTransaction.start() } returns Unit
        mockkObject(RiskLevelTransaction)
        coEvery { RiskLevelTransaction.start() } returns Unit

        coEvery { keyCacheRepository.clear() } returns Unit
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
            exposureNotificationClient = exposureNotificationClient,
            keyCacheRepository = keyCacheRepository
        )

    @Test
    fun `action retrieveDiagnosisKeys, retieves diagnosis keys and calls risklevel calculation`() {
        val vm = createViewModel()

        vm.retrieveDiagnosisKeys()

        coVerifyOrder {
            RetrieveDiagnosisKeysTransaction.start()
            RiskLevelTransaction.start()
        }
    }

    @Test
    fun `action calculateRiskLevel, calls risklevel calculation`() {
        val vm = createViewModel()

        vm.calculateRiskLevel()

        coVerify(exactly = 1) { RiskLevelTransaction.start() }
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
