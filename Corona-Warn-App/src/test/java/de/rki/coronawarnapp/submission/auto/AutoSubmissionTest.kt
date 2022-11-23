package de.rki.coronawarnapp.submission.auto

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.task.SubmissionTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskRequest
import de.rki.coronawarnapp.task.TaskState
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.submitBlocking
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import java.time.Instant

class AutoSubmissionTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var taskController: TaskController

    private val autoSubmissionEnabled: Flow<Boolean> = flowOf(false)
    private val lastSubmissionUserActivityUTC: Flow<Instant> = flowOf(Instant.EPOCH)
    private val autoSubmissionAttemptsCount: Flow<Int> = flowOf(0)
    private val autoSubmissionAttemptsLast: Flow<Instant> = flowOf(Instant.EPOCH)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { workManager.cancelAllWorkByTag(any()) } returns mockk()
        every { workManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()

        every { submissionSettings.autoSubmissionEnabled } returns autoSubmissionEnabled
        every { submissionSettings.lastSubmissionUserActivityUTC } returns lastSubmissionUserActivityUTC
        every { submissionSettings.autoSubmissionAttemptsCount } returns autoSubmissionAttemptsCount
        every { submissionSettings.autoSubmissionAttemptsLast } returns autoSubmissionAttemptsLast

        coEvery { submissionSettings.updateAutoSubmissionEnabled(any()) } just Runs
        coEvery { submissionSettings.updateLastSubmissionUserActivityUTC(any()) } just Runs
        coEvery { submissionSettings.updateAutoSubmissionAttemptsCount(any()) } just Runs
        coEvery { submissionSettings.updateAutoSubmissionAttemptsLast(any()) } just Runs

        every { taskController.tasks } returns emptyFlow()

        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(123456789)

        mockkStatic("de.rki.coronawarnapp.task.TaskControllerExtensionsKt")
    }

    private fun createInstance(appScope: CoroutineScope) = AutoSubmission(
        appScope = appScope,
        timeStamper = timeStamper,
        submissionSettings = submissionSettings,
        workManager = workManager,
        taskController = taskController
    )

    @Test
    fun `init is sideeffect free`() = runTest2 {
        createInstance(this)
        verify { workManager wasNot Called }
    }

    @Test
    fun `update mode DISABLED`() = runTest2 {
        val instance = createInstance(this)

        instance.updateMode(AutoSubmission.Mode.DISABLED)

        coVerifySequence {
            workManager.cancelAllWorkByTag("AutoSubmissionWorker")
            submissionSettings.updateAutoSubmissionEnabled(false)
            submissionSettings.updateLastSubmissionUserActivityUTC(Instant.EPOCH)
            submissionSettings.updateAutoSubmissionAttemptsCount(0)
            submissionSettings.updateAutoSubmissionAttemptsLast(Instant.EPOCH)
        }
    }

    @Test
    fun `update mode MONITOR`() = runTest2 {
        val instance = createInstance(this)
        instance.updateMode(AutoSubmission.Mode.MONITOR)

        coVerifySequence {
            submissionSettings.updateLastSubmissionUserActivityUTC(Instant.ofEpochMilli(123456789))
            submissionSettings.updateAutoSubmissionEnabled(true)

            workManager.enqueueUniquePeriodicWork(
                "AutoSubmissionWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
    }

    @Test
    fun `update mode SUBMIT_ASAP`() = runTest2 {
        val instance = createInstance(this)
        instance.updateMode(AutoSubmission.Mode.SUBMIT_ASAP)

        coVerifySequence {
            submissionSettings.updateLastSubmissionUserActivityUTC(Instant.EPOCH)
            submissionSettings.updateAutoSubmissionEnabled(true)

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
    fun `blocking submission successful`() = runTest2 {
        val instance = createInstance(this)

        val slot = slot<TaskRequest>()

        val taskResult = mockk<TaskState>().apply {
            every { isSuccessful } returns true
        }

        coEvery { taskController.submitBlocking(capture(slot)) } returns taskResult
        instance.runSubmissionNow(BaseCoronaTest.Type.PCR)

        coVerifySequence {
            taskController.submitBlocking(
                DefaultTaskRequest(
                    id = slot.captured.id,
                    type = SubmissionTask::class,
                    arguments = SubmissionTask.Arguments(checkUserActivity = false, testType = BaseCoronaTest.Type.PCR),
                    originTag = "AutoSubmission"
                )
            )
        }
    }

    @Test
    fun `blocking submission failure sets up SUBMIT_ASAP`() = runTest2 {
        val instance = createInstance(this)

        val slot = slot<TaskRequest>()

        val taskResult = mockk<TaskState>().apply {
            every { isSuccessful } returns false
            every { error } returns Exception()
        }

        coEvery { taskController.submitBlocking(capture(slot)) } returns taskResult

        instance.runSubmissionNow(BaseCoronaTest.Type.PCR)

        coVerifySequence {
            submissionSettings.updateLastSubmissionUserActivityUTC(Instant.EPOCH)
            submissionSettings.updateAutoSubmissionEnabled(true)

            workManager.enqueueUniquePeriodicWork(
                "AutoSubmissionWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
    }
}
