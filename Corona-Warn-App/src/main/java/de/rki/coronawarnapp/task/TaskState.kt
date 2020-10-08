package de.rki.coronawarnapp.task

import kotlinx.coroutines.Deferred
import org.joda.time.Instant
import java.util.UUID

data class TaskState(
    val id: UUID = UUID.randomUUID(),
    val request: TaskRequest<*>,
    val createdAt: Instant,
    val startedAt: Instant? = null,
    val completedAt: Instant? = null,
    internal val config: TaskConfig,
    internal val task: Task<*, *>,
    internal val deferred: Deferred<Task.Result>,
    val error: Throwable? = null,
    val result: Task.Result? = null
) {

    val state: State
        get() = when {
            completedAt != null -> State.FINISHED
            deferred.isActive -> State.RUNNING
            else -> State.PENDING
        }

    val type: TaskType
        get() = request.type

    enum class State {
        PENDING,
        RUNNING,
        FINISHED
    }
}
