package de.rki.coronawarnapp.covidcertificate.vaccination.core.execution.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.covidcertificate.vaccination.core.execution.task.VaccinationUpdateTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskFactory
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
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationUpdateWorkerTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK(relaxed = true) lateinit var workerParams: WorkerParameters
    @MockK lateinit var taskController: TaskController
    @MockK lateinit var taskState: TaskState
    private val taskRequestSlot = slot<TaskRequest>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkStatic("de.rki.coronawarnapp.task.TaskControllerExtensionsKt")

        coEvery { taskController.submitBlocking(capture(taskRequestSlot)) } answers { taskState }

        taskState.apply {
            every { isSuccessful } returns true
            every { error } returns null
        }
    }

    private fun createWorker() = VaccinationUpdateWorker(
        context = context,
        workerParams = workerParams,
        taskController = taskController,
    )

    @Test
    fun `sideeffect free`() {
        createWorker()
    }

    @Test
    fun `task is run blockingly`() = runBlockingTest {
        val worker = createWorker()

        worker.doWork() shouldBe ListenableWorker.Result.success()

        taskRequestSlot.captured shouldBe DefaultTaskRequest(
            id = taskRequestSlot.captured.id,
            type = VaccinationUpdateTask::class,
            arguments = VaccinationUpdateTask.Arguments,
            errorHandling = TaskFactory.Config.ErrorHandling.SILENT,
            originTag = "VaccinationUpdateWorker"
        )
    }

    @Test
    fun `task errors are rethrown `() = runBlockingTest {
        taskState.apply {
            every { isSuccessful } returns false
            every { error } returns Exception()
        }

        val worker = createWorker()

        worker.doWork() shouldBe ListenableWorker.Result.retry()

        coVerify {
            taskController.submitBlocking(any())
        }
    }
}
