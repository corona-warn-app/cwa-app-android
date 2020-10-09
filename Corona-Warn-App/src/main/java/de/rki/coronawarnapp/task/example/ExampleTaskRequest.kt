package de.rki.coronawarnapp.task.example

import de.rki.coronawarnapp.task.TaskRequest
import java.util.UUID

data class ExampleTaskRequest(
    override val id: UUID = UUID.randomUUID(),
    override val arguments: ExampleArguments
) : TaskRequest
