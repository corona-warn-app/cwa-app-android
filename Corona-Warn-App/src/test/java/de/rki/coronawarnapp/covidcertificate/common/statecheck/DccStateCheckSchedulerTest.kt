package de.rki.coronawarnapp.covidcertificate.common.statecheck

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureList
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import java.time.Duration
import java.time.Instant

class DccStateCheckSchedulerTest : BaseTest() {
    @MockK lateinit var foregroundState: ForegroundState
    @MockK(relaxed = true) lateinit var workManager: WorkManager
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var dscRepository: DscRepository
    @MockK lateinit var mockDscData: DscSignatureList

    private val isForeground = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { foregroundState.isInForeground } returns isForeground

        dscRepository.apply {
            coEvery { refresh() } just Runs
            every { dscSignatureList } returns flowOf(mockDscData)
        }

        every { mockDscData.updatedAt } returns Instant.EPOCH
        every { timeStamper.nowUTC } returns Instant.ofEpochSecond(1234567)
    }

    fun createInstance(scope: CoroutineScope) = DccStateCheckScheduler(
        appScope = scope,
        foregroundState = foregroundState,
        workManager = workManager,
        dscRepository = dscRepository,
        timeStamper = timeStamper
    )

    @Test
    fun `schedule expiration worker on initialize`() = runTest2 {
        createInstance(this).initialize()

        advanceUntilIdle()

        verify {
            workManager.enqueueUniquePeriodicWork(
                "DccStateCheckWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
    }

    @Test
    fun `refresh dsc data when app comes into foreground`() = runTest2 {
        createInstance(this).apply {
            initialize()

            advanceUntilIdle()

            isForeground.value = false
            advanceUntilIdle()

            coVerify { dscRepository wasNot Called }

            isForeground.value = true
            advanceUntilIdle()
            isForeground.value = true
            advanceUntilIdle()

            isForeground.value = false
            advanceUntilIdle()

            coVerify(exactly = 1) { dscRepository.refresh() }
        }
    }

    @Test
    fun `do not refresh dsc data when last refresh was recent`() = runTest2 {
        every { mockDscData.updatedAt } returns Instant.ofEpochSecond(1234567).minus(Duration.ofHours(12))
        createInstance(this).apply {
            initialize()

            advanceUntilIdle()

            coVerify { dscRepository wasNot Called }

            isForeground.value = true
            advanceUntilIdle()
            isForeground.value = false
            advanceUntilIdle()

            coVerify(exactly = 1) { dscRepository.refresh() }
        }
    }
}
