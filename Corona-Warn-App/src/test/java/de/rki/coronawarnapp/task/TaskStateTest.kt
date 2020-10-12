package de.rki.coronawarnapp.task

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.Deferred
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TaskStateTest : BaseTest() {

    @MockK lateinit var deferred: Deferred<Task.Result>

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `pending state`() {

        val state = InternalTaskState(
            request = mockk(),
            config = mockk(),
            createdAt = mockk(),
            deferred = deferred,
            task = mockk()
        )
        state.executionState shouldBe TaskState.ExecutionState.PENDING

        state.isActive shouldBe true
        state.isFinished shouldBe false
        state.isFailed shouldBe false

        state.isSuccessful shouldBe false
        state.isSkipped shouldBe false
    }

    @Test
    fun `running state`() {
        val state = InternalTaskState(
            request = mockk(),
            config = mockk(),
            createdAt = mockk(),
            startedAt = mockk(),
            deferred = deferred,
            task = mockk()
        )

        state.executionState shouldBe TaskState.ExecutionState.RUNNING

        state.isActive shouldBe true
        state.isFinished shouldBe false

        state.isFailed shouldBe false
        state.isSuccessful shouldBe false
        state.isSkipped shouldBe false
    }

    @Test
    fun `finished state skipped`() {
        val state = InternalTaskState(
            request = mockk(),
            config = mockk(),
            createdAt = mockk(),
            startedAt = mockk(),
            completedAt = mockk(),
            result = null, // skipped
            error = null, // skipped
            deferred = deferred,
            task = mockk()
        )

        state.executionState shouldBe TaskState.ExecutionState.FINISHED

        state.isActive shouldBe false
        state.isFinished shouldBe true

        state.isFailed shouldBe false
        state.isSuccessful shouldBe false
        state.isSkipped shouldBe true
    }

    @Test
    fun `finished state successful`() {
        val state = InternalTaskState(
            request = mockk(),
            config = mockk(),
            createdAt = mockk(),
            startedAt = mockk(),
            completedAt = mockk(),
            result = mockk(), // successful
            error = null,
            deferred = deferred,
            task = mockk()
        )

        state.executionState shouldBe TaskState.ExecutionState.FINISHED

        state.isActive shouldBe false
        state.isFinished shouldBe true

        state.isFailed shouldBe false
        state.isSuccessful shouldBe true
        state.isSkipped shouldBe false
    }

    @Test
    fun `finished state failed`() {
        val state = InternalTaskState(
            request = mockk(),
            config = mockk(),
            createdAt = mockk(),
            startedAt = mockk(),
            completedAt = mockk(),
            result = null,
            error = mockk(), // failed
            deferred = deferred,
            task = mockk()
        )

        state.executionState shouldBe TaskState.ExecutionState.FINISHED

        state.isActive shouldBe false
        state.isFinished shouldBe true

        state.isFailed shouldBe true
        state.isSuccessful shouldBe false
        state.isSkipped shouldBe false
    }
}
