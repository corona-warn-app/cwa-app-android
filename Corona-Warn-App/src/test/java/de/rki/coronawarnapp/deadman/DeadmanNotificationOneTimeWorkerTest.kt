package de.rki.coronawarnapp.deadman

import android.content.Context
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.worker.BackgroundConstants
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class DeadmanNotificationOneTimeWorkerTest : BaseTest() {

    @MockK lateinit var sender: DeadmanNotificationSender
    @MockK lateinit var context: Context
    @RelaxedMockK lateinit var workerParams: WorkerParameters

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createWorker() = DeadmanNotificationOneTimeWorker(
        context = context,
        workerParams = workerParams,
        sender = sender
    )

    @Test
    fun `create worker`() {
        createWorker()
    }

    @Test
    fun `run worker success`() = runBlockingTest2(permanentJobs = true) {
        createWorker().doWork()

        verify(exactly = 1) { sender.sendNotification() }
    }

    @Test
    fun `run worker fail`() = runBlockingTest2(permanentJobs = true) {
        val worker = createWorker()

        worker.runAttemptCount shouldBe 0

        every { worker.runAttemptCount } returns BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD + 1

        worker.doWork()

        verify(exactly = 0) { sender.sendNotification() }
    }
}
