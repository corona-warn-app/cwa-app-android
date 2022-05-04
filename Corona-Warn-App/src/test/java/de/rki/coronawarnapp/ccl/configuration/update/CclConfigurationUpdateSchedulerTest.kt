package de.rki.coronawarnapp.ccl.configuration.update

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2

internal class CclConfigurationUpdateSchedulerTest : BaseTest() {

    @MockK lateinit var foregroundState: ForegroundState
    @MockK(relaxed = true) lateinit var workManager: WorkManager
    @MockK lateinit var cclConfigurationUpdater: CclConfigurationUpdater

    private val isForeground = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { foregroundState.isInForeground } returns isForeground
        coEvery { cclConfigurationUpdater.updateIfRequired() } just Runs
    }

    @Test
    fun `schedule daily worker on setup() call`() = runTest2(ignoreActive = true) {
        createScheduler(this).setup()

        advanceUntilIdle()

        verify {
            workManager.enqueueUniquePeriodicWork(
                "CCLConfigurationUpdateWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
    }

    @Test
    fun `perform update when app comes into foreground`() = runTest2(ignoreActive = true) {
        createScheduler(this).apply {
            setup()

            isForeground.value = false
            advanceUntilIdle()

            coVerify { cclConfigurationUpdater wasNot called }

            // check if cclConfigurationUpdater is only called once even if two true values are emitted
            isForeground.value = true
            advanceUntilIdle()
            isForeground.value = true
            advanceUntilIdle()

            isForeground.value = false
            advanceUntilIdle()

            coVerify(exactly = 1) { cclConfigurationUpdater.updateIfRequired() }
        }
    }

    private fun createScheduler(scope: CoroutineScope): CclConfigurationUpdateScheduler {
        return CclConfigurationUpdateScheduler(scope, foregroundState, cclConfigurationUpdater, workManager)
    }
}
