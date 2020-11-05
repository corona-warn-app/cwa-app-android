package de.rki.coronawarnapp.task.common

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskInfo
import de.rki.coronawarnapp.task.TaskRequest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

class TaskProgressAdapter(
    private val taskController: TaskController,
    private val taskRequest: TaskRequest
) {

    private var alive = true
    private var progressObserverAdded = false

    private fun stopObserving() {
        alive = false
    }

    fun runAnd(callback: (Task.Progress) -> Unit) = runBlocking {
        if (alive) {
            taskController.tasks.collect { list: List<TaskInfo> ->
                list.find { it.taskState.request.id == taskRequest.id }?.also {
                    if (it.taskState.isFinished) {
                        stopObserving()
                    }
                    if (!progressObserverAdded) {
                        progressObserverAdded = true
                        it.progress.collect { progress ->
                            callback.invoke(progress)
                        }
                    }
                }
            }
        }
        taskController.submit(taskRequest)
    }
}
