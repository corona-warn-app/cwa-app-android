package de.rki.coronawarnapp.task

import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TaskControllerTest : BaseTest() {

    private lateinit var coroutineScope: TestCoroutineScope
    @MockK lateinit var taskFactory: TaskFactory<Task.Progress, Task.Result>
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coroutineScope = TestCoroutineScope()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = TaskController(
        taskFactories = mapOf(TaskType.EXAMPLE to taskFactory),
        taskScope = coroutineScope,
        timeStamper = timeStamper
    )

    @Test
    fun `sideeffect free init`() {
        shouldNotThrowAny {
            createInstance()
        }
    }

    @Test
    fun `missing task factory throw exception`() {
        TODO()
    }

    @Test
    fun `task map is empty by default`() {
        TODO()
    }

    @Test
    fun `successful task yields result`() {
        TODO("states show running, finished+result")
    }

    @Test
    fun `failed task yields exception`() {
        TODO("states show finished with exception")
    }

    @Test
    fun `canceled task yields exception`() {
        TODO()
    }

    @Test
    fun `default task execution`() {
        TODO()
    }

    @Test
    fun `queued task execution`() {
        TODO("states show pending task")
    }

    @Test
    fun `parallel task execution`() {
        TODO()
    }

    @Test
    fun `collision behavior only affects task of same type`() {
        TODO()
    }
}
