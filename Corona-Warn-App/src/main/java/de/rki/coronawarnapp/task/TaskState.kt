package de.rki.coronawarnapp.task

import org.joda.time.DateTime
import java.util.UUID

class TaskState(
    val id: UUID,
    val type: TaskType,
    var earliestStart: DateTime
) {
    var state = State.INITIAL
    var error: Throwable? = null
    var lastExecutionTime: DateTime? = null

    enum class State{
        INITIAL,
        PENDING,
        RUNNING,
        FINISHED
    }

}
