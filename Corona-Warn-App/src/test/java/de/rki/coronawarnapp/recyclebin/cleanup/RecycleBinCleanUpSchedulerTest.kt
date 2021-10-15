package de.rki.coronawarnapp.recyclebin.cleanup

import de.rki.coronawarnapp.reyclebin.cleanup.RecycleBinCleanUpScheduler
import de.rki.coronawarnapp.reyclebin.cleanup.RecycleBinCleanUpService
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class RecycleBinCleanUpSchedulerTest : BaseTest() {

    @MockK lateinit var foregroundState: ForegroundState
    @RelaxedMockK lateinit var recycleBinCleanUpService: RecycleBinCleanUpService

    private val isForeground = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { foregroundState.isInForeground } returns isForeground
    }

    fun createInstance(scope: CoroutineScope) = RecycleBinCleanUpScheduler(
        appScope = scope,
        foregroundState = foregroundState,
        recycleBinCleanUpService = recycleBinCleanUpService
    )

    @Test
    fun `start recycle bin clean up when app comes into foreground`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            setup()
            advanceUntilIdle()

            isForeground.value = false
            advanceUntilIdle()

            coVerify { recycleBinCleanUpService wasNot Called }

            isForeground.value = true
            advanceUntilIdle()
            isForeground.value = true
            advanceUntilIdle()

            isForeground.value = false
            advanceUntilIdle()

            coVerify(exactly = 1) { recycleBinCleanUpService.clearRecycledCertificates() }

            isForeground.value = true
            advanceUntilIdle()

            coVerify(exactly = 2) { recycleBinCleanUpService.clearRecycledCertificates() }
        }
    }
}
