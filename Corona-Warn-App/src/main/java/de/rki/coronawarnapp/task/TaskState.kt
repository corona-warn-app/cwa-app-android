package de.rki.coronawarnapp.task

import org.joda.time.Instant
import kotlin.reflect.KClass

interface TaskState {
    val request: TaskRequest
    val createdAt: Instant
    val startedAt: Instant?
    val completedAt: Instant?
    val error: Throwable?
    val result: Task.Result?
    val state: State
    val type: KClass<out Task<*, *>>

    val isFinished: Boolean
        get() = state == State.FINISHED

    val isFailed: Boolean
        get() = isFinished && error != null

    val isSkipped: Boolean
        get() = isFinished && result == null && error == null

    val isSuccessful: Boolean
        get() = isFinished && result != null

    val isActive: Boolean
        get() = !isFinished

    val resultOrThrow: Task.Result
        get() {
            return when {
                isActive -> throw IllegalStateException("Task is still running.")
                isSuccessful -> result!!
                isFailed -> throw error!!
                isSkipped -> throw IllegalStateException("Task was skipped.")
                else -> throw IllegalStateException("Unknown task state")
            }
        }

    enum class State {
        PENDING,
        RUNNING,
        FINISHED
    }
}
