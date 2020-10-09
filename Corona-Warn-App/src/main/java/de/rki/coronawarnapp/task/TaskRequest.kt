package de.rki.coronawarnapp.task

import java.util.UUID

interface TaskRequest {
    val id: UUID
    val arguments: Task.Arguments
}
