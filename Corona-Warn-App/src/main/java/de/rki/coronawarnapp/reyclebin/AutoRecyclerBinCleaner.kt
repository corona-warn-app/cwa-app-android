package de.rki.coronawarnapp.reyclebin

import de.rki.coronawarnapp.reyclebin.task.RecycleBinCleanTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import javax.inject.Inject

class AutoRecyclerBinCleaner @Inject constructor(
    private val taskController: TaskController
) {

    fun run() {
        taskController.submit(
            DefaultTaskRequest(
                RecycleBinCleanTask::class,
                originTag = "RecycleBinCleanTaskRun"
            )
        )
    }
}
