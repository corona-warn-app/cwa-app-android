package de.rki.coronawarnapp.covidcertificate.booster

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class BoosterCheckSchedulerTest : BaseTest() {

    @RelaxedMockK lateinit var workManager: WorkManager
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var boosterNotificationService: BoosterNotificationService

    private val isForeground = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { foregroundState.isInForeground } returns isForeground
        coEvery { boosterNotificationService.checkBoosterNotification() } just runs
    }

    private fun createInstance(scope: CoroutineScope) = BoosterCheckScheduler(
        appScope = scope,
        foregroundState = foregroundState,
        workManager = workManager,
        boosterNotificationService = boosterNotificationService
    )

    @Test
    fun `schedule booster check on setup`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).setup()

        advanceUntilIdle()

        verify {
            workManager.enqueueUniquePeriodicWork(
                "BoosterCheckWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
    }

    @Test
    fun `force booster check when app comes into foreground`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            setup()

            advanceUntilIdle()

            isForeground.value = false
            advanceUntilIdle()

            coVerify { boosterNotificationService wasNot Called }

            isForeground.value = true
            advanceUntilIdle()
            isForeground.value = true
            advanceUntilIdle()

            isForeground.value = false
            advanceUntilIdle()

            coVerify(exactly = 1) { boosterNotificationService.checkBoosterNotification() }
        }
    }
}
