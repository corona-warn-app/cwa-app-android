package de.rki.coronawarnapp.task.common

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskRequest
import java.util.UUID
import kotlin.reflect.KClass

data class DefaultTaskRequest(
    override val type: KClass<out Task<Task.Progress, Task.Result>>,
    override val arguments: Task.Arguments = object : Task.Arguments {},
    override val id: UUID = UUID.randomUUID()
) : TaskRequest {

    fun toNewTask(): DefaultTaskRequest = copy(id = UUID.randomUUID())
}
