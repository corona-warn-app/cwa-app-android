package de.rki.coronawarnapp.vaccination.core.execution

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskRequest
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.execution.task.VaccinationUpdateTask
import de.rki.coronawarnapp.vaccination.core.execution.worker.VaccinationUpdateWorkerRequestBuilder
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.gms.MockListenableFuture

class VaccinationUpdateSchedulerTest : BaseTest() {

    @MockK lateinit var taskController: TaskController
    @MockK lateinit var vaccinationRepository: VaccinationRepository
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var workerRequestBuilder: VaccinationUpdateWorkerRequestBuilder
    @MockK lateinit var workInfo: WorkInfo
    @MockK lateinit var periodicWorkRequest: PeriodicWorkRequest
    @MockK lateinit var timeStamper: TimeStamper

    private val nowUTC = Instant.ofEpochSecond(1611764225)

    private val vaccinationInfosFlow = MutableStateFlow(emptySet<VaccinatedPerson>())
    private val foregroundStateFlow = MutableStateFlow(false)
    private val taskRequestSlot = slot<TaskRequest>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        // Happy path is no proofs, no executions / workers
        every { taskController.submit(any()) } just Runs
        every { timeStamper.nowUTC } returns nowUTC

        every { workerRequestBuilder.createPeriodicWorkRequest() } returns periodicWorkRequest

        workManager.apply {
            every { enqueueUniquePeriodicWork(any(), any(), periodicWorkRequest) } returns mockk()
            every { cancelUniqueWork(any()) } returns mockk()
            every { getWorkInfosForUniqueWork(any()) } returns MockListenableFuture.forResult(listOf(workInfo))
        }

        every { workInfo.state } returns WorkInfo.State.SUCCEEDED

        vaccinationRepository.apply {
            every { vaccinationInfos } returns vaccinationInfosFlow
        }

        every { foregroundState.isInForeground } returns foregroundStateFlow

        every { taskController.submit(any()) } just Runs
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

    private fun mockPerson(
        isEligbleForPC: Boolean,
        hasPendingCheck: Boolean = false,
        lastProofCheckTime: Instant = Instant.now()
    ): VaccinatedPerson = mockk<VaccinatedPerson>().apply {
        every { isEligbleForProofCertificate } returns isEligbleForPC
        every { isProofCertificateCheckPending } returns hasPendingCheck
        every { lastProofCheckAt } returns lastProofCheckTime
    }

    @Test
    fun `the worker is canceled if there is no elligble vaccination certificate`() =
        runBlockingTest2(ignoreActive = true) {
            val instance = createInstance(scope = this)
            instance.setup()

            verify {
                workManager.cancelUniqueWork("VaccinationUpdateWorker")
            }
        }

    @Test
    fun `any pending proofs cause the worker to be scheduled`() = runBlockingTest2(ignoreActive = true) {
        vaccinationInfosFlow.value = setOf(mockPerson(hasPendingCheck = true, isEligbleForPC = true))

        createInstance(scope = this).setup()

        verify {
            workManager.enqueueUniquePeriodicWork(
                "VaccinationUpdateWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }
    }

    @Test
    fun `reaching foreground state with pending proofs causes immediate refresh`() =
        runBlockingTest2(ignoreActive = true) {
            vaccinationInfosFlow.value = setOf(mockPerson(hasPendingCheck = true, isEligbleForPC = true))

            createInstance(scope = this).setup()

            verify(exactly = 0) { taskController.submit(any()) }

            foregroundStateFlow.value = true

            advanceUntilIdle()

            verify { taskController.submit(capture(taskRequestSlot)) }

            taskRequestSlot.captured.apply {
                type shouldBe VaccinationUpdateTask::class
                errorHandling shouldBe TaskFactory.Config.ErrorHandling.SILENT
            }
        }

    @Test
    fun `reaching foreground state with stale proof data causes immediate refresh`() =
        runBlockingTest2(ignoreActive = true) {
            vaccinationInfosFlow.value = setOf(mockPerson(isEligbleForPC = true, lastProofCheckTime = Instant.EPOCH))

            createInstance(scope = this).setup()

            verify(exactly = 0) { taskController.submit(any()) }

            foregroundStateFlow.value = true

            advanceUntilIdle()

            verify { taskController.submit(capture(taskRequestSlot)) }

            taskRequestSlot.captured.apply {
                type shouldBe VaccinationUpdateTask::class
                errorHandling shouldBe TaskFactory.Config.ErrorHandling.SILENT
            }
        }
}
