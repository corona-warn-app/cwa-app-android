package de.rki.coronawarnapp.task

import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.task.example.QueueingTask
import de.rki.coronawarnapp.task.example.SkippingTask
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
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope
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
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID

class TaskControllerTest : BaseIOTest() {

    private val taskFactoryMap: MutableMap<
        Class<out Task<Task.Progress, Task.Result>>,
        TaskFactory<out Task.Progress, out Task.Result>
        > = mutableMapOf()
    @MockK lateinit var timeStamper: TimeStamper

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        val factory = QueueingTask.Factory { QueueingTask() }
        taskFactoryMap[QueueingTask::class.java] = spyk(factory)

        val factory2 = SkippingTask.Factory { SkippingTask() }
        taskFactoryMap[SkippingTask::class.java] = spyk(factory2)

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
            arguments = arguments,
            type = QueueingTask::class
        )

        arguments.path.exists() shouldBe false

        instance.submit(request)

        val infoRunning = instance.tasks.first().single()
        infoRunning.apply {
            taskState.state shouldBe TaskState.State.RUNNING
            taskState.startedAt!!.isAfter(taskState.createdAt) shouldBe true

            taskState.isActive shouldBe true

            shouldThrowAny {
                taskState.resultOrThrow shouldBe null
            }
        }
        val progressCollector = infoRunning.progress.test(scope = this)

        this.advanceUntilIdle()

        val infoFinished = instance.tasks
            .first { it.single().taskState.state == TaskState.State.FINISHED }
            .single()

        arguments.path.exists() shouldBe true

        val lastProgressMessage = progressCollector.values().last().primaryMessage.get(mockk())
        lastProgressMessage shouldBe arguments.values.last()

        infoFinished.apply {
            // No more progress, task is finished
//            progress.first() shouldBe ""

            taskState.isSuccessful shouldBe true
            taskState.resultOrThrow shouldNotBe null

            taskState.startedAt!!.isAfter(taskState.createdAt) shouldBe true
            taskState.completedAt!!.isAfter(taskState.startedAt) shouldBe true

            taskState.error shouldBe null

            (taskState.result as QueueingTask.Result).apply {
                writtenBytes shouldBe arguments.path.length()
            }
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
            arguments = arguments,
            type = QueueingTask::class
        )

        arguments.path.exists() shouldBe false

        // The target path is now a directory, this will fail the task
        arguments.path.mkdirs()

        instance.submit(request)

        this.advanceUntilIdle()

        val infoFinished = instance.tasks
            .first { it.single().taskState.state == TaskState.State.FINISHED }
            .single()

        infoFinished.apply {
            taskState.startedAt!!.isAfter(taskState.createdAt) shouldBe true
            taskState.completedAt!!.isAfter(taskState.startedAt) shouldBe true

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
            arguments = arguments,
            type = QueueingTask::class
        )
        instance.submit(request)
        delay(1000)
        instance.cancel(request.id)

        val infoFinished = instance.tasks
            .first { it.single().taskState.state == TaskState.State.FINISHED }
            .single()

        infoFinished.taskState.error shouldBe instanceOf(TaskCancelationException::class)

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
            arguments = arguments,
            type = QueueingTask::class
        )
        instance.submit(request1)

        val request2 = request1.copy(id = UUID.randomUUID())
        instance.submit(request2)

        val infoPending = instance.tasks.first { emission ->
            emission.any { it.taskState.state == TaskState.State.PENDING }
        }
        infoPending.size shouldBe 2
        infoPending.single { it.taskState.request == request1 }.apply {
            taskState.state == TaskState.State.RUNNING
        }
        infoPending.single { it.taskState.request == request2 }.apply {
            taskState.state == TaskState.State.PENDING
        }

        this.advanceUntilIdle()

        val infoFinished = instance.tasks.first { emission ->
            emission.any { it.taskState.state == TaskState.State.FINISHED }
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
            arguments = arguments,
            type = SkippingTask::class
        )
        instance.submit(request1)

        val request2 = DefaultTaskRequest(
            arguments = arguments,
            type = SkippingTask::class
        )
        instance.submit(request2)

        this.advanceUntilIdle()

        val infoFinished = instance.tasks.first { emission ->
            emission.any { it.taskState.state == TaskState.State.FINISHED }
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
            arguments = arguments,
            type = QueueingTask::class
        )

        // Class needs to be different, typing is based on that.
        val request2 = DefaultTaskRequest(
            arguments = arguments,
            type = SkippingTask::class
        )

        val instance = createInstance(scope = this)

        instance.submit(request1)
        instance.submit(request2)

        this.advanceUntilIdle()

        val infoFinished = instance.tasks.first { emission ->
            emission.any { it.taskState.state == TaskState.State.FINISHED }
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

        instance.close()
    }

    @Test
    fun `resubmitting a request has no effect`() = runBlockingTest {
        val instance = createInstance(scope = this)

        val arguments = QueueingTask.Arguments(
            path = File(testDir, UUID.randomUUID().toString())
        )
        val request = DefaultTaskRequest(
            arguments = arguments,
            type = QueueingTask::class
        )

        arguments.path.exists() shouldBe false

        instance.submit(request)
        instance.submit(request)

        val infoFinished = instance.tasks
            .first { it.single().taskState.state == TaskState.State.FINISHED }
            .single()

        infoFinished.apply {
            (taskState.resultOrThrow as QueueingTask.Result).apply {
                writtenBytes shouldBe arguments.path.length()
            }
        }

        instance.tasks.first().size shouldBe 1

        instance.close()
    }
}
