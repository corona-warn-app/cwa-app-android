package de.rki.coronawarnapp.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import java.time.Instant

class ForegroundRiskCalculationServiceTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var tracingRepository: TracingRepository
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var powerManager: PowerManager
    @MockK lateinit var wifiManager: WifiManager
    @MockK lateinit var wakeLock: PowerManager.WakeLock
    @MockK lateinit var wifiLock: WifiManager.WifiLock

    private val foregroundFlow = MutableStateFlow(true)
    private val onboardedFlow = MutableStateFlow(false)

    private fun createService(scope: CoroutineScope) = ForegroundRiskCalculationService(
        context, scope, tracingRepository, foregroundState, scope, timeStamper, onboardingSettings
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { foregroundState.isInForeground } returns foregroundFlow
        every { onboardingSettings.isOnboardedFlow } returns onboardedFlow
        every { timeStamper.nowUTC } returnsMany listOf(
            Instant.ofEpochMilli(10000000000),
            Instant.ofEpochMilli(20000000000),
            Instant.ofEpochMilli(30000000000),
            Instant.ofEpochMilli(40000000000),
            Instant.ofEpochMilli(50000000000),
        )
        every { tracingRepository.runRiskCalculations() } returns Job()
        coEvery { context.getSystemService(Context.POWER_SERVICE) } returns powerManager
        every { context.applicationContext } returns context
        every { context.getSystemService(Context.WIFI_SERVICE) } returns wifiManager
        every { powerManager.newWakeLock(any(), any()) } returns wakeLock
        every { wakeLock.acquire(any()) } just Runs
        every { wakeLock.release() } just Runs
        every { wifiManager.createWifiLock(any(), any()) } returns wifiLock
        every { wifiLock.acquire() } just Runs
        every { wifiLock.release() } just Runs
        every { wakeLock.isHeld } returns true
        every { wifiLock.isHeld } returns true
    }

    @Test
    fun `runs on app start`() = runTest2 {
        createService(this).initialize()
        onboardedFlow.emit(true)

        advanceUntilIdle()

        coVerify(exactly = 1) {
            tracingRepository.runRiskCalculations()
        }
    }

    @Test
    fun `runs on every first change to foreground`() = runTest2 {
        createService(this).initialize()

        foregroundFlow.emit(true)
        onboardedFlow.emit(true)
        foregroundFlow.emit(true)
        foregroundFlow.emit(false)
        foregroundFlow.emit(true)
        foregroundFlow.emit(true)

        advanceUntilIdle()
        coVerify(exactly = 2) {
            tracingRepository.runRiskCalculations()
        }
    }

    @Test
    fun `runs at most once an hour`() = runTest2 {
        every { timeStamper.nowUTC } returnsMany listOf(
            Instant.ofEpochMilli(10000000000),
            Instant.ofEpochMilli(10000000001),
            Instant.ofEpochMilli(10000000002),
            Instant.ofEpochMilli(10000000003),
        )

        createService(this).initialize()
        onboardedFlow.emit(true)
        foregroundFlow.emit(true)
        foregroundFlow.emit(false)
        foregroundFlow.emit(true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            tracingRepository.runRiskCalculations()
        }
    }

    @Test
    fun `wait until user is onboarded`() = runTest2 {
        every { timeStamper.nowUTC } returnsMany listOf(
            Instant.ofEpochMilli(10000000000),
            Instant.ofEpochMilli(10000000001),
            Instant.ofEpochMilli(10000000002),
            Instant.ofEpochMilli(10000000003),
        )

        createService(this).initialize()
        foregroundFlow.emit(true)

        advanceUntilIdle()
        coVerify(exactly = 0) {
            tracingRepository.runRiskCalculations()
        }

        onboardedFlow.emit(true)

        advanceUntilIdle()
        coVerify(exactly = 1) {
            tracingRepository.runRiskCalculations()
        }
    }
}
