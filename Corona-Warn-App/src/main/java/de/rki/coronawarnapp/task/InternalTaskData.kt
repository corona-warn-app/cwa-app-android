package de.rki.coronawarnapp.task

import de.rki.coronawarnapp.task.TaskData.State
import kotlinx.coroutines.Deferred
import org.joda.time.Instant
import java.util.UUID

internal data class InternalTaskData(
    override val id: UUID = UUID.randomUUID(),
    override val request: TaskRequest<*>,
    override val createdAt: Instant,
    override val startedAt: Instant? = null,
    override val completedAt: Instant? = null,
    override val error: Throwable? = null,
    override val result: Task.Result? = null,
    internal val config: TaskConfig,
    internal val deferred: Deferred<Task.Result>,
    internal val task: Task<*, *>
) : TaskData {

    override val state: State
        get() = when {
            completedAt != null -> State.FINISHED
            deferred.isActive -> State.RUNNING
            else -> State.PENDING
        }

    override val type: TaskType
        get() = request.type
}
