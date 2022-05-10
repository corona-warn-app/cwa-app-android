package de.rki.coronawarnapp.deadman

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.storage.OnboardingSettings
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var enfClient: ENFClient

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { workBuilder.buildPeriodicWork() } returns periodicWorkRequest
        every { workBuilder.buildOneTimeWork(any()) } returns oneTimeWorkRequest
        every { workManager.cancelUniqueWork(DeadmanNotificationScheduler.PERIODIC_WORK_NAME) } returns operation
        every { workManager.cancelUniqueWork(DeadmanNotificationScheduler.ONE_TIME_WORK_NAME) } returns operation
        every {
            workManager.enqueueUniquePeriodicWork(
                DeadmanNotificationScheduler.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
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

        every { onboardingSettings.isOnboardedFlow } returns flowOf(true)
        every { enfClient.isTracingEnabled } returns flowOf(true)
    }

    private fun createScheduler(scope: CoroutineScope) = DeadmanNotificationScheduler(
        appScope = scope,
        timeCalculation = timeCalculation,
        workManager = workManager,
        workBuilder = workBuilder,
        onboardingSettings = onboardingSettings,
        enfClient = enfClient
    )

    @Test
    fun `one time work was scheduled`() = runTest(UnconfinedTestDispatcher()) {
        coEvery { timeCalculation.getDelayInMinutes() } returns 10L

        createScheduler(this).scheduleOneTime()

        verifySequence {
            workManager.enqueueUniqueWork(
                DeadmanNotificationScheduler.ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )
        }
    }

    @Test
    fun `one time work was not scheduled`() = runTest(UnconfinedTestDispatcher()) {
        coEvery { timeCalculation.getDelayInMinutes() } returns -10L

        createScheduler(this).scheduleOneTime()

        verify(exactly = 0) {
            workManager.enqueueUniqueWork(
                DeadmanNotificationScheduler.ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeWorkRequest
            )
        }

        verify(exactly = 0) {
            workManager.enqueueUniquePeriodicWork(
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `test periodic work was scheduled`() = runTest(UnconfinedTestDispatcher()) {
        createScheduler(this).schedulePeriodic()

        verifyPeriodicWorkScheduled()
    }

    @Test
    fun `scheduled work should be cancelled if onboarding wasn't yet done `() = runTest(UnconfinedTestDispatcher()) {
        every { onboardingSettings.isOnboardedFlow } returns flowOf(false)

        createScheduler(this).apply { setup() }

        verifyCancelScheduledWork()
        verifyPeriodicWorkScheduled(exactly = 0)
    }

    @Test
    fun `scheduled work should be cancelled if tracing is disabled`() = runTest(UnconfinedTestDispatcher()) {
        every { enfClient.isTracingEnabled } returns flowOf(false)

        createScheduler(this).apply { setup() }

        verifyCancelScheduledWork()
        verifyPeriodicWorkScheduled(exactly = 0)
    }

    private fun verifyPeriodicWorkScheduled(exactly: Int = 1) {
        verify(exactly = exactly) {
            workManager.enqueueUniquePeriodicWork(
                DeadmanNotificationScheduler.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }
    }

    private fun verifyCancelScheduledWork() {
        verify(exactly = 1) {
            workManager.cancelUniqueWork(DeadmanNotificationScheduler.PERIODIC_WORK_NAME)
        }
        verify(exactly = 1) {
            workManager.cancelUniqueWork(DeadmanNotificationScheduler.ONE_TIME_WORK_NAME)
        }
    }
}
