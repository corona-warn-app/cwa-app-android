package de.rki.coronawarnapp.task

import de.rki.coronawarnapp.task.TaskConfig.ExecutionMode
import de.rki.coronawarnapp.transaction.TransactionCoroutineScope
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskController @Inject constructor(
    private val taskFactories: @JvmSuppressWildcards Map<TaskType, TaskFactory<out Task.Progress, out Task.Result>>,
    private val taskScope: TransactionCoroutineScope,
    private val timeStamper: TimeStamper
) {

    private val mutex = Mutex()
    private val taskQueue = Channel<TaskRequest<*>>(Channel.UNLIMITED)
    private val internalStates = MutableStateFlow<Map<UUID, TaskState>>(
        emptyMap()
    )
    val taskStates: Flow<Map<TaskState, Flow<Task.Progress>>> = internalStates
        .map { it.values }
        .map { states ->
            states.map { it to it.task.progress }.toMap()
        }

    init {
        Timber.d("We have factories for %s", taskFactories.keys)

        taskQueue
            .receiveAsFlow()
            .onEach {
                Timber.d("Processing new task request: %s", it)
                initTaskState(it)
            }
            .launchIn(taskScope)
    }

    fun submitTask(request: TaskRequest<*>) {
        Timber.tag(TAG).i("Task submitted: %s", request)
        taskQueue.offer(request)
    }

    private suspend fun initTaskState(request: TaskRequest<*>) {
        val taskFactory = taskFactories[request.type]
        requireNotNull(taskFactory) { "No factory available for $request" }
        val taskConfig = taskFactory.config
        val task = taskFactory.taskProvider()

        internalStates.updateSafely {
            val deferred = taskScope.async(
                start = CoroutineStart.LAZY
            ) { task.run(request.arguments) }

            val activeTask = TaskState(
                request = request,
                createdAt = timeStamper.nowUTC,
                config = taskConfig,
                task = task,
                deferred = deferred
            )
            put(activeTask.id, activeTask)
        }
        taskScope.launch { processMap() }
    }

    private suspend fun processMap() = internalStates.updateSafely {
        // Procress all unprocessed finished tasks
        values.toList()
            .filter { it.deferred.isCompleted && it.state != TaskState.State.FINISHED }
            .forEach { state ->
                val error = state.deferred.getCompletionExceptionOrNull()
                val result = if (error == null) {
                    state.deferred.getCompleted()
                } else {
                    Timber.tag(TAG).e(error, "Task failed: %s", state)
                    null
                }

                this[state.id] = state.copy(
                    completedAt = timeStamper.nowUTC,
                    result = result,
                    error = error
                )
            }

        // Start new tasks
        values.toList()
            .filter { it.state == TaskState.State.PENDING }
            .forEach { state ->
                val siblingTasks = values.filter {
                    it.type == state.request.type
                        && it.state != TaskState.State.FINISHED
                        && it.id != state.id
                }
                Timber.tag(TAG).d("Sibling tasks for %s are: %s", state, siblingTasks)

                // Handle **[ExecutionMode]** here
                when {
                    siblingTasks.isEmpty() || state.config.executionMode == ExecutionMode.PARALLEL -> {
                        this[state.id] = state.toRunningState()
                    }
                    state.config.executionMode == ExecutionMode.ENQUEUE -> {
                        Timber.tag(TAG).d("Postponing task %s", state)
                    }
                }
            }
    }

    private fun TaskState.toRunningState(): TaskState {
        deferred.invokeOnCompletion {
            Timber.tag(TAG).i("Task completed (successfully=%b)", it != null)
            taskScope.launch { processMap() }
        }
        deferred.start()
        return copy(startedAt = timeStamper.nowUTC).also {
            Timber.tag(TAG).d("Starting new task: %s", it)
        }
    }

    private suspend fun <K, V> MutableStateFlow<Map<K, V>>.updateSafely(
        update: suspend MutableMap<K, V>.() -> Unit
    ) = mutex.withLock {
        val mutableMap = value.toMutableMap()
        update(mutableMap)
        value = mutableMap
    }

    companion object {
        private const val TAG = "TaskController"
    }
}
