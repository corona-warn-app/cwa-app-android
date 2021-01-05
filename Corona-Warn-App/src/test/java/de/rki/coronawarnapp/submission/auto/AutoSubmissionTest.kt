package de.rki.coronawarnapp.submission.auto

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.task.SubmissionTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskRequest
import de.rki.coronawarnapp.task.TaskState
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.preferences.FlowPreference
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference

class AutoSubmissionTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var taskController: TaskController

    private val autoSubmissionEnabled: FlowPreference<Boolean> = mockFlowPreference(false)
    private val lastSubmissionUserActivityUTC: FlowPreference<Instant> = mockFlowPreference(Instant.EPOCH)
    private val autoSubmissionAttemptsCount: FlowPreference<Int> = mockFlowPreference(0)
    private val autoSubmissionAttemptsLast: FlowPreference<Instant> = mockFlowPreference(Instant.EPOCH)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { workManager.cancelAllWorkByTag(any()) } returns mockk()
        every { workManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()

        every { submissionSettings.autoSubmissionEnabled } returns autoSubmissionEnabled
        every { submissionSettings.lastSubmissionUserActivityUTC } returns lastSubmissionUserActivityUTC
        every { submissionSettings.autoSubmissionAttemptsCount } returns autoSubmissionAttemptsCount
        every { submissionSettings.autoSubmissionAttemptsLast } returns autoSubmissionAttemptsLast

        every { taskController.tasks } returns emptyFlow()

        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(123456789)

        mockkStatic("de.rki.coronawarnapp.task.TaskControllerExtensionsKt")
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = AutoSubmission(
        timeStamper = timeStamper,
        submissionSettings = submissionSettings,
        workManager = workManager,
        taskController = taskController
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()

        verify { workManager wasNot Called }
    }

    @Test
    fun `update mode DISABLED`() {
        val instance = createInstance()

        instance.updateMode(AutoSubmission.Mode.DISABLED)

        verifySequence {
            workManager.cancelAllWorkByTag("AutoSubmissionWorker")
            autoSubmissionEnabled.update(match { !it.invoke(true) })
            lastSubmissionUserActivityUTC.update(match { it.invoke(Instant.now()) == Instant.EPOCH })
            autoSubmissionAttemptsCount.update(match { it.invoke(123) == 0 })
            autoSubmissionAttemptsLast.update(match { it.invoke(Instant.now()) == Instant.EPOCH })
        }
    }

    @Test
    fun `update mode MONITOR`() {
        val instance = createInstance()

        instance.updateMode(AutoSubmission.Mode.MONITOR)

        verifySequence {
            lastSubmissionUserActivityUTC.update(match { it.invoke(Instant.now()) == Instant.ofEpochMilli(123456789) })
            autoSubmissionEnabled.update(match { it.invoke(false) })

            workManager.enqueueUniquePeriodicWork(
                "AutoSubmissionWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
    }

    @Test
    fun `update mode SUBMIT_ASAP`() {
        val instance = createInstance()

        instance.updateMode(AutoSubmission.Mode.SUBMIT_ASAP)

        verifySequence {
            lastSubmissionUserActivityUTC.update(match { it.invoke(Instant.now()) == Instant.EPOCH })
            autoSubmissionEnabled.update(match { it.invoke(false) })

            workManager.enqueueUniquePeriodicWork(
                "AutoSubmissionWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                match {
                    it.workSpec.constraints.requiredNetworkType == NetworkType.CONNECTED &&
                        it.workSpec.intervalDuration == 15 * 60 * 1000L
                }
            )
        }
    }

    @Test
    fun `blocking submission successful`() {
        val instance = createInstance()

        val slot = slot<TaskRequest>()

        val taskResult = mockk<TaskState>().apply {
            every { isSuccessful } returns true
        }

        coEvery { taskController.submitBlocking(capture(slot)) } returns taskResult

        runBlockingTest {
            instance.runSubmissionNow()
        }

        coVerifySequence {
            taskController.submitBlocking(
                DefaultTaskRequest(
                    id = slot.captured.id,
                    type = SubmissionTask::class,
                    arguments = SubmissionTask.Arguments(checkUserActivity = false),
                    originTag = "AutoSubmission"
                )
            )
        }
    }

    @Test
    fun `blocking submission failure sets up SUBMIT_ASAP`() {
        val instance = createInstance()

        val slot = slot<TaskRequest>()

        val taskResult = mockk<TaskState>().apply {
            every { isSuccessful } returns false
            every { error } returns Exception()
        }

        coEvery { taskController.submitBlocking(capture(slot)) } returns taskResult

        runBlockingTest {
            instance.runSubmissionNow()
        }

        verifySequence {
            lastSubmissionUserActivityUTC.update(match { it.invoke(Instant.now()) == Instant.EPOCH })
            autoSubmissionEnabled.update(match { it.invoke(false) })

            workManager.enqueueUniquePeriodicWork(
                "AutoSubmissionWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
    }
}
