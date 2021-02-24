package de.rki.coronawarnapp.contactdiary.retention

import android.content.Context
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.task.TaskController
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ContactDiaryCleanWorkerTest : BaseTest() {

    @MockK lateinit var context: Context
    @RelaxedMockK lateinit var workerParams: WorkerParameters
    @RelaxedMockK lateinit var taskController: TaskController

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createWorker() = ContactDiaryRetentionWorker(
        context = context,
        workerParams = workerParams,
        taskController = taskController
    )

    @Test
    fun `create worker`() {
        createWorker()
    }
}
