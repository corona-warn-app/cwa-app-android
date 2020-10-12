package de.rki.coronawarnapp.task

import org.joda.time.Instant
import kotlin.reflect.KClass

interface TaskData {
    val request: TaskRequest
    val createdAt: Instant
    val startedAt: Instant?
    val completedAt: Instant?
    val error: Throwable?
    val result: Task.Result?
    val state: State
    val type: KClass<out TaskRequest>

    enum class State {
        PENDING,
        RUNNING,
        FINISHED
    }
}
