package de.rki.coronawarnapp.task

import kotlinx.coroutines.flow.Flow

data class TaskInfo(
    val taskState: TaskState,
    val progress: Flow<Task.Progress>
)
