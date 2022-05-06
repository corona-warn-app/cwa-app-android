package de.rki.coronawarnapp.recyclebin.cleanup

import de.rki.coronawarnapp.reyclebin.cleanup.RecycleBinCleanUpScheduler
import de.rki.coronawarnapp.reyclebin.cleanup.RecycleBinCleanUpService
import de.rki.coronawarnapp.util.device.ForegroundState
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2

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
    fun `start recycle bin clean up when app comes into foreground`() = runTest2 {
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

            coVerify(exactly = 1) { recycleBinCleanUpService.clearRecycledItems() }

            isForeground.value = true
            advanceUntilIdle()

            coVerify(exactly = 2) { recycleBinCleanUpService.clearRecycledItems() }
        }
    }

    @Test
    fun `clean up errors won't break scheduling`() = runTest2 {
        coEvery { recycleBinCleanUpService.clearRecycledItems() } throws Exception("Test error")

        createInstance(this).run {
            setup()
            advanceUntilIdle()

            shouldNotThrowAny {
                isForeground.value = true
                advanceUntilIdle()
            }

            coEvery { recycleBinCleanUpService.clearRecycledItems() } just runs

            isForeground.value = false
            advanceUntilIdle()
            isForeground.value = true
            advanceUntilIdle()

            coVerify(exactly = 2) { recycleBinCleanUpService.clearRecycledItems() }
        }
    }
}
