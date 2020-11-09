package de.rki.coronawarnapp.deadman

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DeadmanNotificationSchedulerTest : BaseTest() {

    @MockK lateinit var timeCalculation: DeadmanNotificationTimeCalculation
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var operation: Operation
    @MockK lateinit var workBuilder: DeadmanNotificationWorkBuilder
    @MockK lateinit var periodicWorkRequest: PeriodicWorkRequest
    @MockK lateinit var oneTimeWorkRequest: OneTimeWorkRequest

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { workBuilder.buildPeriodicWork() } returns periodicWorkRequest
        every { workBuilder.buildOneTimeWork(any()) } returns oneTimeWorkRequest
        every {
            workManager.enqueueUniquePeriodicWork(
                DeadmanNotificationScheduler.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                any()
            )
        } returns operation

        every {
            workManager.enqueueUniqueWork(
                DeadmanNotificationScheduler.ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )
        } returns operation
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createScheduler() = DeadmanNotificationScheduler(
        timeCalculation = timeCalculation,
        workManager = workManager,
        workBuilder = workBuilder
    )

    @Test
    fun `one time work was scheduled`() = runBlockingTest {
        coEvery { timeCalculation.getDelay() } returns 10L

        createScheduler().scheduleOneTime()

        verifySequence {
            workManager.enqueueUniqueWork(
                DeadmanNotificationScheduler.ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )
        }
    }

    @Test
    fun `one time work was not scheduled`() = runBlockingTest {
        coEvery { timeCalculation.getDelay() } returns -10L

        createScheduler().scheduleOneTime()

        verify(exactly = 0) {
            workManager.enqueueUniqueWork(
                DeadmanNotificationScheduler.ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )
        }

        verify(exactly = 0) {
            workManager.enqueueUniquePeriodicWork(
                any(), any(), any()
            )
        }
    }

    @Test
    fun `test periodic work was scheduled`() {
        createScheduler().schedulePeriodic()

        verifySequence {
            workManager.enqueueUniquePeriodicWork(
                DeadmanNotificationScheduler.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )
        }
    }
}
