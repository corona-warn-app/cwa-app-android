package de.rki.coronawarnapp.covidcertificate.common.statecheck

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import de.rki.coronawarnapp.covidcertificate.signature.core.DscData
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
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class DccStateCheckSchedulerTest : BaseTest() {
    @MockK lateinit var foregroundState: ForegroundState
    @MockK(relaxed = true) lateinit var workManager: WorkManager
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var dscRepository: DscRepository
    @MockK lateinit var mockDscData: DscData

    private val isForeground = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { foregroundState.isInForeground } returns isForeground

        dscRepository.apply {
            coEvery { refresh() } just Runs
            every { dscData } returns flowOf(mockDscData)
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
    fun `schedule expiration worker on setup`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).setup()

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
    fun `refresh dsc data when app comes into foreground`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).apply {
            setup()

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
    fun `do not refresh dsc data when last refresh was recent`() = runBlockingTest2(ignoreActive = true) {
        every { mockDscData.updatedAt } returns Instant.ofEpochSecond(1234567).minus(Duration.standardHours(12))
        createInstance(this).apply {
            setup()

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
