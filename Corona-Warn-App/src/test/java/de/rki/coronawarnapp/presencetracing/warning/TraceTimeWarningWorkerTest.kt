package de.rki.coronawarnapp.presencetracing.warning

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.presencetracing.warning.worker.PresenceTracingWarningTask
import de.rki.coronawarnapp.presencetracing.warning.worker.PresenceTracingWarningWorker
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskRequest
import de.rki.coronawarnapp.task.TaskState
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TraceTimeWarningWorkerTest : BaseTest() {

    @RelaxedMockK lateinit var workerParams: WorkerParameters
    @MockK lateinit var context: Context
    @MockK lateinit var taskController: TaskController
    @MockK(relaxed = true) lateinit var taskResult: TaskState

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkStatic("de.rki.coronawarnapp.task.TaskControllerExtensionsKt")

        coEvery { taskController.submitBlocking(any()) } returns taskResult
    }

    private fun createWorker() = PresenceTracingWarningWorker(
        context = context,
        workerParams = workerParams,
        taskController = taskController
    )

    @Test
    fun `worker runs task with user activity check enabled`() = runBlockingTest {
        every { taskResult.error } returns null
        val slot = slot<TaskRequest>()
        coEvery { taskController.submitBlocking(capture(slot)) } returns taskResult

        val worker = createWorker()

        worker.doWork() shouldBe ListenableWorker.Result.success()

        slot.captured shouldBe DefaultTaskRequest(
            id = slot.captured.id,
            type = PresenceTracingWarningTask::class,
            originTag = "TraceTimeWarningWorker"
        )
    }

    @Test
    fun `task errors are rethrown `() = runBlockingTest {
        every { taskResult.error } returns Exception()

        val worker = createWorker()

        worker.doWork() shouldBe ListenableWorker.Result.retry()

        coVerify {
            taskController.submitBlocking(any())
        }
    }
}
