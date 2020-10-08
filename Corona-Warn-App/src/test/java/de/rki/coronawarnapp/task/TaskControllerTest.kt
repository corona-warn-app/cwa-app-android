package de.rki.coronawarnapp.task

import de.rki.coronawarnapp.transaction.TransactionCoroutineScope
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import testhelpers.BaseTest

class TaskControllerTest : BaseTest() {

    @Mock lateinit var taskFactories: Map<TaskType, TaskFactory<out Task.Progress, out Task.Result>>
    @Mock lateinit var taskScope: TransactionCoroutineScope
    @Mock lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = TaskController(
        taskFactories = taskFactories,
        taskScope = taskScope,
        timeStamper = timeStamper
    )

    @Test
    fun `sideeffect free init`() {
        shouldNotThrowAny {
            createInstance()
        }
    }
}
