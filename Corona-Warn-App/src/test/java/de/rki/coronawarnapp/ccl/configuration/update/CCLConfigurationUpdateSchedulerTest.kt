package de.rki.coronawarnapp.ccl.configuration.update

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

internal class CCLConfigurationUpdateSchedulerTest : BaseTest() {

    @MockK lateinit var foregroundState: ForegroundState
    @MockK(relaxed = true) lateinit var workManager: WorkManager
    @MockK lateinit var cclConfigurationUpdater: CCLConfigurationUpdater

    private val isForeground = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { foregroundState.isInForeground } returns isForeground
        coEvery { cclConfigurationUpdater.updateIfRequired() } returns true
    }

    @Test
    fun `schedule daily worker on setup() call`() = runBlockingTest2(ignoreActive = true) {
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
    fun `perform update when app comes into foreground`() = runBlockingTest2(ignoreActive = true) {
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

            coVerify(exactly = 1) { cclConfigurationUpdater }
        }
    }

    private fun createScheduler(scope: CoroutineScope): CCLConfigurationUpdateScheduler {
        return CCLConfigurationUpdateScheduler(scope, foregroundState, cclConfigurationUpdater, workManager)
    }
}
