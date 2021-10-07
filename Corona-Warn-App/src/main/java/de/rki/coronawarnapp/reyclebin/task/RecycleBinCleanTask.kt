package de.rki.coronawarnapp.reyclebin.task

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class RecycleBinCleanTask @Inject constructor(
    private val recycleBinRetentionCalculation: RecycleBinRetentionCalculation
) : Task<DefaultProgress, Task.Result> {
    override val progress: Flow<DefaultProgress> = emptyFlow()

    override suspend fun run(arguments: Task.Arguments): Task.Result {
        // TODO
        return object : Task.Result {}
    }

    override suspend fun cancel() {
        // TODO
    }
}
