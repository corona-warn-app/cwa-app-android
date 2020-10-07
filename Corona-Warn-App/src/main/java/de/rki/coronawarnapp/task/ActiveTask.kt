package de.rki.coronawarnapp.task

import org.joda.time.Instant
import java.util.UUID

data class ActiveTask(
    val id: UUID,
    val type: TaskType,
    val createdAt: Instant,
    val state: State = State.PENDING,
    val error: Throwable? = null
) {

    enum class State {
        PENDING,
        RUNNING,
        FINISHED
    }
}
