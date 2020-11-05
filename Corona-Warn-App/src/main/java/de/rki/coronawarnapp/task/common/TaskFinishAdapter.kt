package de.rki.coronawarnapp.task.common

import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskInfo
import de.rki.coronawarnapp.task.TaskRequest
import de.rki.coronawarnapp.task.TaskState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

class TaskFinishAdapter(
    private val taskController: TaskController,
    private val taskRequest: TaskRequest
) {

    private var alive = true

    private fun stopObserving() {
        alive = false
    }

    fun runAndThen(callback: (TaskState) -> Unit) = runBlocking {
        taskController.tasks.collect { list: List<TaskInfo> ->
            if (alive) {
                list.find { it.taskState.request.id == taskRequest.id }?.taskState?.also {
                    if (it.isFinished) {
                        stopObserving()
                        callback.invoke(it)
                    }
                }
            }
        }
        taskController.submit(taskRequest)
    }
}
