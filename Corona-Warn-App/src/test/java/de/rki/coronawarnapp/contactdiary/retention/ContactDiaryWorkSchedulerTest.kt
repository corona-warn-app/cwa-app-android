package de.rki.coronawarnapp.contactdiary.retention

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import de.rki.coronawarnapp.storage.OnboardingSettings
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2

class ContactDiaryWorkSchedulerTest : BaseTest() {

    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var operation: Operation
    @MockK lateinit var workBuilder: ContactDiaryWorkBuilder
    @MockK lateinit var periodicWorkRequest: PeriodicWorkRequest
    @MockK lateinit var onBoardingSettings: OnboardingSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { workBuilder.buildPeriodicWork() } returns periodicWorkRequest
        every {
            workManager.enqueueUniquePeriodicWork(
                ContactDiaryWorkScheduler.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                any()
            )
        } returns operation
    }

    private fun createScheduler(scope: CoroutineScope) = ContactDiaryWorkScheduler(
        appScope = scope,
        workManager = workManager,
        workBuilder = workBuilder,
        onboardingSettings = onBoardingSettings
    )

    @Test
    fun `test periodic work was scheduled`() = runTest {
        createScheduler(this).schedulePeriodic()
        verifyIfWorkWasScheduled()
    }

    @Test
    fun `periodic work should be scheduled after onboaring`() = runTest2 {
        val onboardingFlow = MutableStateFlow(false)
        every { onBoardingSettings.isOnboardedFlow } returns onboardingFlow
        createScheduler(this).initialize()
        verifyIfWorkWasScheduled(exactly = 0)

        onboardingFlow.value = true
        verifyIfWorkWasScheduled(exactly = 1)
    }

    private fun verifyIfWorkWasScheduled(exactly: Int = 1) {
        verify(exactly = exactly) {
            workManager.enqueueUniquePeriodicWork(
                ContactDiaryWorkScheduler.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )
        }
    }
}
