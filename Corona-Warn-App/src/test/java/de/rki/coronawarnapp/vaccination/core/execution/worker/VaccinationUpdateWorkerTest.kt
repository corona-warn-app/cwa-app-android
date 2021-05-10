package de.rki.coronawarnapp.vaccination.core.execution.worker

import android.content.Context
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.task.TaskController
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationUpdateWorkerTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var workerParams: WorkerParameters
    @MockK lateinit var taskController: TaskController

    private fun createInstance() = VaccinationUpdateWorker(
        context = context,
        workerParams = workerParams,
        taskController = taskController,
    )

    @Test
    fun `to do`() {
        TODO()
    }
}
