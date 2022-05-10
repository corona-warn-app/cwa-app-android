package de.rki.coronawarnapp.deadman

import android.content.Context
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.worker.BackgroundConstants
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DeadmanNotificationPeriodicWorkerTest : BaseTest() {

    @MockK lateinit var scheduler: DeadmanNotificationScheduler
    @MockK lateinit var context: Context
    @RelaxedMockK lateinit var workerParams: WorkerParameters

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createWorker() = DeadmanNotificationPeriodicWorker(
        context = context,
        workerParams = workerParams,
        scheduler = scheduler
    )

    @Test
    fun `create worker`() {
        createWorker()
    }

    @Test
    fun `run worker success`() = runTest {
        createWorker().doWork()

        coVerify(exactly = 1) { scheduler.scheduleOneTime() }
    }

    @Test
    fun `run worker fail`() = runTest {
        val worker = createWorker()

        worker.runAttemptCount shouldBe 0

        every { worker.runAttemptCount } returns BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD + 1

        worker.doWork()

        coVerify(exactly = 0) { scheduler.scheduleOneTime() }
    }
}
