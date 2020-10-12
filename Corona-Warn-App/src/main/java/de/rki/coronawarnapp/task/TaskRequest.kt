package de.rki.coronawarnapp.task

import java.util.UUID
import kotlin.reflect.KClass

interface TaskRequest {
    val id: UUID
    val type: KClass<out Task<Task.Progress, Task.Result>>
    val arguments: Task.Arguments
}
