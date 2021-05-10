package de.rki.coronawarnapp.vaccination.core.execution

import androidx.work.WorkManager
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.vaccination.core.execution.worker.VaccinationUpdateWorkerRequestBuilder
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationUpdateSchedulerTest : BaseTest() {

    @MockK lateinit var taskController: TaskController
    @MockK lateinit var vaccinationRepository: VaccinationRepository
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var workerRequestBuilder: VaccinationUpdateWorkerRequestBuilder
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
    }

    private fun createInstance(scope: CoroutineScope) = VaccinationUpdateScheduler(
        appScope = scope,
        taskController = taskController,
        vaccinationRepository = vaccinationRepository,
        foregroundState = foregroundState,
        workManager = workManager,
        workerRequestBuilder = workerRequestBuilder,
        timeStamper = timeStamper,
    )

    @Test
    fun `to do`() {
        TODO()
    }
}
