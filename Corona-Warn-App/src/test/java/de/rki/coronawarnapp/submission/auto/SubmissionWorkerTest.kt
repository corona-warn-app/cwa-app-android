package de.rki.coronawarnapp.submission.auto

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.submission.task.SubmissionTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskRequest
import de.rki.coronawarnapp.task.TaskState
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SubmissionWorkerTest : BaseTest() {

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

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createWorker() = SubmissionWorker(
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
            type = SubmissionTask::class,
            arguments = SubmissionTask.Arguments(checkUserActivity = true),
            errorHandling = TaskFactory.Config.ErrorHandling.SILENT,
            originTag = "SubmissionWorker"
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
