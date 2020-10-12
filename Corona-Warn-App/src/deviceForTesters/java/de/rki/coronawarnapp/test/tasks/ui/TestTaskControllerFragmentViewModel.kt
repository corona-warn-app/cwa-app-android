package de.rki.coronawarnapp.test.tasks.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskState
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.test.tasks.testtask.TestTask
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class TestTaskControllerFragmentViewModel @AssistedInject constructor(
    private val taskController: TaskController,
    private val dispatcherProvider: DispatcherProvider,
    private val taskFactories: Map<
        @JvmSuppressWildcards Class<out Task<*, *>>,
        @JvmSuppressWildcards TaskFactory<out Task.Progress, out Task.Result>>,
) : CWAViewModel() {

    data class FactoryState(
        val infos: List<String>
    )

    val factoryState: LiveData<FactoryState> = liveData(context = dispatcherProvider.Default) {
        val infoStrings = taskFactories.map {
            val taskLabel = it.key.simpleName
            val collisionBehavior = it.value.config.collisionBehavior.toString()
            """
                $taskLabel - Behavior: $collisionBehavior
            """.trimIndent()
        }
        emit(FactoryState(infos = infoStrings))
    }

    data class AllTasksState(
        val stateDescriptions: List<String>
    )

    val controllerState = taskController.tasks.map { states ->
        var counter = 0
        val taskStates = states
            .map { it.taskState }
            .filter { it.isActive }
            .sortedBy { it.createdAt }

        AllTasksState(
            stateDescriptions = taskStates.map {
                val id = it.request.id.toString()
                val type = it.type.simpleName!!
                val createdAt = it.createdAt.toString()

                val resultType = when {
                    it.executionState == TaskState.ExecutionState.PENDING -> "Pending"
                    it.isActive -> "Running"
                    else -> "UNKNOWN"
                }
                """
                        #${counter++} $type - $resultType
                        $id
                        Created: $createdAt
                    """.trimIndent()
            }
        )
    }.asLiveData(dispatcherProvider.Default)

    data class LastActivityState(
        val lastActivity: List<String>
    )

    val lastActivityState = taskController.tasks.map { states ->
        val taskStates = states.map { it.taskState }.sortedBy { it.createdAt }

        taskFactories.keys.map { type ->
            val typeLabel = type.simpleName
            val count = taskStates.count { it.type.java == type }
            val lastTask = taskStates.lastOrNull { it.type.java == type }
            val completedAt = lastTask?.completedAt
            val resultType = when {
                lastTask == null -> "Never ran"
                lastTask.isActive -> "Running!"
                lastTask.isFailed -> "Failed"
                lastTask.isSuccessful -> "Successful"
                lastTask.isSkipped -> "Skipped"
                else -> "UNKNOWN"
            }
            """
                $typeLabel - $count times
                $resultType at $completedAt
            """.trimIndent()
        }.let { LastActivityState(it) }
    }.asLiveData(dispatcherProvider.Default)

    val latestTestTaskProgress = taskController.tasks.flatMapMerge { states ->
        val latestTestTaskInfo = states
            .sortedBy { it.taskState.createdAt }
            .lastOrNull { it.taskState.type == TestTask::class }
        latestTestTaskInfo?.progress ?: flowOf(null)
    }.asLiveData(dispatcherProvider.Default)

    private var testTaskCounter = 0

    fun launchTestTask() {
        taskController.submit(
            DefaultTaskRequest(
                type = TestTask::class,
                arguments = TestTask.Arguments("${testTaskCounter++}")
            )
        )
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<TestTaskControllerFragmentViewModel>
}
