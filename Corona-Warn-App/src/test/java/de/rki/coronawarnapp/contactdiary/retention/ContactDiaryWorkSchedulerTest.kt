package de.rki.coronawarnapp.contactdiary.retention

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verifySequence
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ContactDiaryWorkSchedulerTest : BaseTest() {

    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var operation: Operation
    @MockK lateinit var workBuilder: ContactDiaryWorkBuilder
    @MockK lateinit var periodicWorkRequest: PeriodicWorkRequest

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

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createScheduler() = ContactDiaryWorkScheduler(
        workManager = workManager,
        workBuilder = workBuilder
    )

    @Test
    fun `test periodic work was scheduled`() {
        createScheduler().schedulePeriodic()

        verifySequence {
            workManager.enqueueUniquePeriodicWork(
                ContactDiaryWorkScheduler.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )
        }
    }
}
