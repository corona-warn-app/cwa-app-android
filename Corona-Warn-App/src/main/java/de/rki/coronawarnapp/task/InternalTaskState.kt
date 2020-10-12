package de.rki.coronawarnapp.task

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
    override val completedAt: Instant? = null,
    override val error: Throwable? = null,
    override val result: Task.Result? = null,
    internal val config: TaskFactory.Config,
    internal val deferred: Deferred<Task.Result>,
    internal val task: Task<*, *>
) : TaskState {

    override val type: KClass<out Task<*, *>>
        get() = task::class

    override val executionState: ExecutionState
        get() = when {
            completedAt != null -> ExecutionState.FINISHED
            startedAt != null -> ExecutionState.RUNNING
            else -> ExecutionState.PENDING
        }
}
