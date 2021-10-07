package de.rki.coronawarnapp.reyclebin

import de.rki.coronawarnapp.task.TaskController
import javax.inject.Inject

class AutoRecyclerBinCleaner @Inject constructor(
    private val taskController: TaskController
) {

    fun run() {
        /* TODO
        taskController.submit(
            DefaultTaskRequest(
                RecycleBinCleanTask::class,
                originTag = "RecycleBinCleanTaskRun"
            )
        )*/
    }
}
