package de.rki.coronawarnapp.task.internal

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskRequest
import de.rki.coronawarnapp.task.TaskState
import de.rki.coronawarnapp.task.TaskState.ExecutionState
import kotlinx.coroutines.Deferred
import org.joda.time.Instant
import java.util.UUID
import kotlin.reflect.KClass

internal data class InternalTaskState(
    internal val id: UUID = UUID.randomUUID(),
    override val request: TaskRequest,
    override val createdAt: Instant,
    override val startedAt: Instant? = null,
    override val finishedAt: Instant? = null,
    override val error: Throwable? = null,
    override val result: Task.Result? = null,
    internal val config: TaskFactory.Config,
    internal val job: Deferred<Task.Result>,
    internal val task: Task<*, *>
) : TaskState {

    override val type: KClass<out Task<*, *>>
        get() = task::class

    override val executionState: ExecutionState
        get() = when {
            finishedAt != null -> ExecutionState.FINISHED
            startedAt != null -> ExecutionState.RUNNING
            else -> ExecutionState.PENDING
        }

    fun toLogString(): String = """
    ${request.type.simpleName} state=${executionState.name} id=$id 
        startedAt=$startedAt finishedAt=$finishedAt result=${result != null} error=$error
        arguments=${request.arguments} config=$config
    """.trimIndent()
}
