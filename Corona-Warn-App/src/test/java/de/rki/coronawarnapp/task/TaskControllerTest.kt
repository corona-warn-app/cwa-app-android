package de.rki.coronawarnapp.task

import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.example.QueueingTask
import de.rki.coronawarnapp.task.testtasks.SkippingTask
import de.rki.coronawarnapp.task.testtasks.timeout.TimeoutTask
import de.rki.coronawarnapp.task.testtasks.timeout.TimeoutTask2
import de.rki.coronawarnapp.task.testtasks.timeout.TimeoutTaskArguments
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.instanceOf
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.coroutines.test
import testhelpers.extensions.isAfterOrEqual
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID
import javax.inject.Provider

class TaskControllerTest : BaseIOTest() {

    private val taskFactoryMap: MutableMap<
        Class<out Task<Task.Progress, Task.Result>>,
        TaskFactory<out Task.Progress, out Task.Result>
        > = mutableMapOf()
    @MockK lateinit var timeStamper: TimeStamper

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)

    private val timeoutFactory = spyk(TimeoutTask.Factory(Provider { TimeoutTask() }))
    private val timeoutFactory2 = spyk(TimeoutTask2.Factory(Provider { TimeoutTask2() }))
    private val queueingFactory = spyk(QueueingTask.Factory(Provider { QueueingTask() }))
    private val skippingFactory = spyk(SkippingTask.Factory(Provider { SkippingTask() }))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        taskFactoryMap[QueueingTask::class.java] = queueingFactory
        taskFactoryMap[SkippingTask::class.java] = skippingFactory
        taskFactoryMap[TimeoutTask::class.java] = timeoutFactory
        taskFactoryMap[TimeoutTask2::class.java] = timeoutFactory2

        every { timeStamper.nowUTC } answers {
            Instant.now()
        }
    }

    @AfterEach
    fun teardown() {
        taskFactoryMap.clear()
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createInstance(scope: CoroutineScope) = TaskController(
        taskFactories = taskFactoryMap,
        taskScope = scope,
        timeStamper = timeStamper
    )

    @Test
    fun `sideeffect free init`() = runBlockingTest {
        shouldNotThrowAny {
            val instance = createInstance(scope = this)
            instance.close()
        }
    }

    @Test
    fun `missing task factory throw exception`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val unknownTask = DefaultTaskRequest(
            type = Task::class,
            arguments = mockk()
        )

        shouldThrow<MissingTaskFactoryException> {
            instance.submit(unknownTask)
        }

        instance.close()
    }

    @Test
    fun `task map is empty by default`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val map = instance.tasks.take(1).toList().single()
        map shouldBe emptyList()

        instance.close()
    }

    @Test
    fun `default task execution`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val arguments = QueueingTask.Arguments(
            path = File(testDir, UUID.randomUUID().toString())
        )
        val request = DefaultTaskRequest(
            type = QueueingTask::class,
            arguments = arguments
        )

        arguments.path.exists() shouldBe false

        instance.submit(request)

        val infoRunning = instance.tasks.first().single()
        infoRunning.apply {
            taskState.executionState shouldBe TaskState.ExecutionState.RUNNING
            taskState.startedAt!!.isAfterOrEqual(taskState.createdAt) shouldBe true

            taskState.isActive shouldBe true

            shouldThrowAny {
                taskState.resultOrThrow shouldBe null
            }
        }
        val progressCollector = infoRunning.progress.test(startOnScope = this)

        this.advanceUntilIdle()

        val infoFinished = instance.tasks
            .first { it.single().taskState.executionState == TaskState.ExecutionState.FINISHED }
            .single()

        arguments.path.exists() shouldBe true

        val lastProgressMessage = progressCollector.latestValue!!.primaryMessage.get(mockk())
        lastProgressMessage shouldBe arguments.values.last()

        infoFinished.apply {
            // No more progress, task is finished
//            progress.first() shouldBe ""

            taskState.isSuccessful shouldBe true
            taskState.resultOrThrow shouldNotBe null

            taskState.startedAt!!.isAfterOrEqual(taskState.createdAt) shouldBe true
            taskState.finishedAt!!.isAfterOrEqual(taskState.startedAt!!) shouldBe true

            taskState.error shouldBe null

            (taskState.result as QueueingTask.Result).apply {
                writtenBytes shouldBe arguments.path.length()
            }
        }

        coVerifySequence {
            queueingFactory.createConfig()
            queueingFactory.taskProvider
        }

        instance.close()
    }

    @Test
    fun `failed task yields exception`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val arguments = QueueingTask.Arguments(
            path = File(testDir, UUID.randomUUID().toString())
        )
        val request = DefaultTaskRequest(
            type = QueueingTask::class,
            arguments = arguments
        )

        arguments.path.exists() shouldBe false

        // The target path is now a directory, this will fail the task
        arguments.path.mkdirs()

        instance.submit(request)

        this.advanceUntilIdle()

        val infoFinished = instance.tasks
            .first { it.single().taskState.executionState == TaskState.ExecutionState.FINISHED }
            .single()

        infoFinished.apply {
            taskState.startedAt!!.isAfterOrEqual(taskState.createdAt) shouldBe true
            taskState.finishedAt!!.isAfterOrEqual(taskState.startedAt!!) shouldBe true

            taskState.isSuccessful shouldBe false
            taskState.isFailed shouldBe true

            taskState.result shouldBe null
            taskState.error should instanceOf(FileNotFoundException::class)
        }

        instance.close()
    }

    @Test
    fun `canceled task yields exception`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val arguments = QueueingTask.Arguments(
            path = File(testDir, UUID.randomUUID().toString())
        )
        val request = DefaultTaskRequest(
            type = QueueingTask::class,
            arguments = arguments
        )
        instance.submit(request)
        delay(1000)
        instance.cancel(request.id)

        val infoFinished = instance.tasks
            .first { it.single().taskState.executionState == TaskState.ExecutionState.FINISHED }
            .single()

        infoFinished.taskState.error shouldBe instanceOf(TaskCancellationException::class)

        instance.close()
    }

    @Test
    fun `queued task execution`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val arguments = QueueingTask.Arguments(
            path = File(testDir, UUID.randomUUID().toString())
        )
        arguments.path.exists() shouldBe false

        val request1 = DefaultTaskRequest(
            type = QueueingTask::class,
            arguments = arguments
        )
        instance.submit(request1)

        val request2 = request1.toNewTask()
        instance.submit(request2)

        val infoPending = instance.tasks.first { emission ->
            emission.any { it.taskState.executionState == TaskState.ExecutionState.PENDING }
        }
        infoPending.size shouldBe 2
        infoPending.single { it.taskState.request == request1 }.apply {
            taskState.executionState shouldBe TaskState.ExecutionState.RUNNING
        }
        infoPending.single { it.taskState.request == request2 }.apply {
            taskState.executionState shouldBe TaskState.ExecutionState.PENDING
        }

        this.advanceUntilIdle()

        val infoFinished = instance.tasks.first { emission ->
            emission.any { it.taskState.executionState == TaskState.ExecutionState.FINISHED }
        }
        infoFinished.size shouldBe 2

        // Let's make sure both tasks actually ran
        infoFinished.single { it.taskState.request == request2 }.apply {
            val result = taskState.resultOrThrow as QueueingTask.Result
            arguments.path.length() shouldBe result.writtenBytes
        }
        infoFinished.single { it.taskState.request == request1 }.apply {
            val result = taskState.resultOrThrow as QueueingTask.Result
            arguments.path.length() shouldNotBe result.writtenBytes
        }

        arguments.path.length() shouldBe 720L

        instance.close()
    }

    @Test
    fun `skippable tasks are skipped`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val arguments = QueueingTask.Arguments(
            path = File(testDir, UUID.randomUUID().toString())
        )
        arguments.path.exists() shouldBe false

        val request1 = DefaultTaskRequest(
            type = SkippingTask::class,
            arguments = arguments
        )
        instance.submit(request1)

        val request2 = DefaultTaskRequest(
            type = SkippingTask::class,
            arguments = arguments
        )
        instance.submit(request2)

        this.advanceUntilIdle()

        val infoFinished = instance.tasks.first { emission ->
            emission.any { it.taskState.executionState == TaskState.ExecutionState.FINISHED }
        }
        infoFinished.size shouldBe 2

        infoFinished.single { it.taskState.request == request1 }.apply {
            taskState.type shouldBe SkippingTask::class
            taskState.isSkipped shouldBe false
            taskState.resultOrThrow shouldNotBe null
        }
        infoFinished.single { it.taskState.request == request2 }.apply {
            taskState.type shouldBe SkippingTask::class
            taskState.isSkipped shouldBe true
            taskState.result shouldBe null
            taskState.error shouldBe null
        }

        arguments.path.length() shouldBe 360L

        instance.close()
    }

    @Test
    fun `collision behavior only affects task of same type`() = runBlockingTest {
        val arguments = QueueingTask.Arguments(path = File(testDir, UUID.randomUUID().toString()))
        arguments.path.exists() shouldBe false

        val request1 = DefaultTaskRequest(
            type = QueueingTask::class,
            arguments = arguments
        )

        // Class needs to be different, typing is based on that.
        val request2 = DefaultTaskRequest(
            type = SkippingTask::class,
            arguments = arguments
        )

        val instance = createInstance(scope = this)

        instance.submit(request1)
        instance.submit(request2)

        this.advanceUntilIdle()

        val infoFinished = instance.tasks.first { emission ->
            emission.any { it.taskState.executionState == TaskState.ExecutionState.FINISHED }
        }
        infoFinished.size shouldBe 2

        infoFinished.single { it.taskState.request == request1 }.apply {
            taskState.isSkipped shouldBe false
            taskState.resultOrThrow shouldNotBe null
        }
        infoFinished.single { it.taskState.request == request2 }.apply {
            taskState.isSkipped shouldBe false
            taskState.resultOrThrow shouldNotBe null
        }

        arguments.path.length() shouldBe 720L

        coVerifySequence {
            queueingFactory.createConfig()
            queueingFactory.taskProvider
            skippingFactory.createConfig()
            skippingFactory.taskProvider
        }

        instance.close()
    }

    @Test
    fun `resubmitting a request has no effect`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val arguments = QueueingTask.Arguments(
            path = File(testDir, UUID.randomUUID().toString())
        )
        val request = DefaultTaskRequest(
            type = QueueingTask::class,
            arguments = arguments
        )

        arguments.path.exists() shouldBe false

        instance.submit(request)
        instance.submit(request)

        val infoFinished = instance.tasks
            .first { it.single().taskState.executionState == TaskState.ExecutionState.FINISHED }
            .single()

        infoFinished.apply {
            (taskState.resultOrThrow as QueueingTask.Result).apply {
                writtenBytes shouldBe arguments.path.length()
            }
        }

        instance.tasks.first().size shouldBe 1

        instance.close()
    }

    @Test
    fun `tasks are timed out according to their config`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val request = DefaultTaskRequest(
            type = TimeoutTask::class,
            arguments = TimeoutTaskArguments()
        )

        instance.submit(request)

        val infoFinished = instance.tasks
            .first { it.single().taskState.executionState == TaskState.ExecutionState.FINISHED }
            .single()

        infoFinished.apply {
            taskState.isFailed shouldBe true
            taskState.error shouldBe instanceOf(TimeoutCancellationException::class)
        }

        instance.tasks.first().size shouldBe 1

        instance.close()
    }

    @Test
    fun `timeout starts on execution, not while pending`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val taskWithTimeout = DefaultTaskRequest(
            type = TimeoutTask::class,
            arguments = TimeoutTaskArguments()
        )
        val taskWithoutTimeout = DefaultTaskRequest(
            type = TimeoutTask::class,
            arguments = TimeoutTaskArguments(delay = 5000)
        )
        val taskWithoutTimeout2 = taskWithoutTimeout.toNewTask()

        instance.submit(taskWithTimeout)
        instance.submit(taskWithoutTimeout)
        instance.submit(taskWithoutTimeout2)

        val finishedTasks = instance.tasks.first { tasks ->
            tasks.all { it.taskState.executionState == TaskState.ExecutionState.FINISHED }
        }
        instance.tasks.first().size shouldBe 3

        finishedTasks.single { it.taskState.request == taskWithTimeout }.apply {
            taskState.isFailed shouldBe true
            taskState.error shouldBe instanceOf(TimeoutCancellationException::class)
        }
        finishedTasks.single { it.taskState.request == taskWithoutTimeout }.apply {
            taskState.isSuccessful shouldBe true
            taskState.error shouldBe null
            taskState.result shouldNotBe null
        }
        finishedTasks.single { it.taskState.request == taskWithoutTimeout2 }.apply {
            taskState.isSuccessful shouldBe true
            taskState.error shouldBe null
            taskState.result shouldNotBe null
        }

        instance.close()
    }

    @Test
    fun `parallel tasks can timeout`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val task1WithTimeout = DefaultTaskRequest(
            type = TimeoutTask::class,
            arguments = TimeoutTaskArguments()
        )
        val task2WithTimeout = DefaultTaskRequest(
            type = TimeoutTask2::class,
            arguments = TimeoutTaskArguments()
        )
        val task1WithoutTimeout = DefaultTaskRequest(
            type = TimeoutTask::class,
            arguments = TimeoutTaskArguments(delay = 5000)
        )
        val task2WithoutTimeout = DefaultTaskRequest(
            type = TimeoutTask2::class,
            arguments = TimeoutTaskArguments(delay = 5000)
        )

        instance.submit(task1WithTimeout)
        instance.submit(task2WithTimeout)
        instance.submit(task1WithoutTimeout)
        instance.submit(task2WithoutTimeout)

        val finishedTasks = instance.tasks.first { tasks ->
            tasks.all { it.taskState.executionState == TaskState.ExecutionState.FINISHED }
        }
        instance.tasks.first().size shouldBe 4

        finishedTasks.single { it.taskState.request == task1WithTimeout }.apply {
            taskState.isFailed shouldBe true
            taskState.error shouldBe instanceOf(TimeoutCancellationException::class)
        }
        finishedTasks.single { it.taskState.request == task2WithTimeout }.apply {
            taskState.isFailed shouldBe true
            taskState.error shouldBe instanceOf(TimeoutCancellationException::class)
        }
        finishedTasks.single { it.taskState.request == task1WithoutTimeout }.apply {
            taskState.isSuccessful shouldBe true
            taskState.error shouldBe null
            taskState.result shouldNotBe null
        }
        finishedTasks.single { it.taskState.request == task2WithoutTimeout }.apply {
            taskState.isSuccessful shouldBe true
            taskState.error shouldBe null
            taskState.result shouldNotBe null
        }

        instance.close()
    }
}
