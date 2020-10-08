package de.rki.coronawarnapp.task

import java.util.UUID

data class TaskRequest<ArgT : Task.Arguments>(
    val id: UUID = UUID.randomUUID(),
    val type: TaskType,
    val arguments: ArgT
)
