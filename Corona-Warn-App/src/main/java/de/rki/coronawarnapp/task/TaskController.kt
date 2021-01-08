package de.rki.coronawarnapp.task

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.task.TaskFactory.Config.CollisionBehavior
import de.rki.coronawarnapp.task.internal.InternalTaskState
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskController @Inject constructor(
    private val taskFactories: Map<
        @JvmSuppressWildcards Class<out Task<*, *>>,
        @JvmSuppressWildcards TaskFactory<out Task.Progress, out Task.Result>>,
    @TaskCoroutineScope private val taskScope: CoroutineScope,
    private val timeStamper: TimeStamper
) {

    private val mutex = Mutex()
    private val taskQueue = Channel<TaskRequest>(Channel.UNLIMITED)
    private val internalTaskData = MutableStateFlow<Map<UUID, InternalTaskState>>(
        emptyMap()
    )

    private val submissionProcessor = taskQueue
        .receiveAsFlow()
        .onStart { Timber.tag(TAG).v("Listening to task request queue.") }
        .onEach { initTaskData(it) }
        .onCompletion { Timber.tag(TAG).w("Stopped listening to request queue. Why?") }
        .launchIn(taskScope)

    val tasks: Flow<List<TaskInfo>> = internalTaskData
        .map { it.values }
        .map { tasks ->
            tasks.map {
                TaskInfo(it, it.task.progress)
            }
        }

    init {
        Timber.tag(TAG).d("We have factories for %s", taskFactories.keys)
    }

    /**
     * Don't re-use taskrequests, create new requests for each submission.
     * They contain unique IDs.
     */
    fun submit(request: TaskRequest) {
        if (!taskFactories.containsKey(request.type.java)) {
            throw MissingTaskFactoryException(request::class)
        }
        Timber.tag(TAG).i("Task submitted: %s", request)
        taskQueue.offer(request)
    }

    suspend fun cancel(requestId: UUID) = internalTaskData.updateSafely {
        val taskState = values.singleOrNull {
            it.request.id == requestId
        } ?: throw IllegalArgumentException("No task found for request ID $requestId")

        Timber.tag(TAG).w("Manually canceling %s", taskState)
        taskState.job.cancel(cause = TaskCancellationException())
    }

    private suspend fun initTaskData(newRequest: TaskRequest) {
        Timber.tag(TAG).d("Processing new task request: %s", newRequest)
        val taskFactory = taskFactories[newRequest.type.java]
        requireNotNull(taskFactory) { "No factory available for $newRequest" }

        Timber.tag(TAG).v("Initiating task data for request: %s", newRequest)
        val taskConfig = taskFactory.createConfig()
        val task = taskFactory.taskProvider()

        val deferred = taskScope.async(start = CoroutineStart.LAZY) {
            withTimeout(timeMillis = taskConfig.executionTimeout.millis) {
                task.run(newRequest.arguments)
            }
        }

        val activeTask = InternalTaskState(
            request = newRequest,
            createdAt = timeStamper.nowUTC,
            config = taskConfig,
            task = task,
            job = deferred
        )

        internalTaskData.updateSafely {
            val existingRequest = values.singleOrNull { it.request.id == newRequest.id }
            if (existingRequest == null) {
                Timber.tag(TAG).d("Added new pending task: %s", activeTask)
                put(activeTask.id, activeTask)
            } else {
                Timber.tag(TAG).w(
                    "TaskRequest was already used. Existing: %s\nNew request: %s",
                    existingRequest, newRequest
                )
            }
        }
        taskScope.launch { processMap() }
    }

    private suspend fun processMap() = internalTaskData.updateSafely {
        Timber.tag(TAG).d("Processing task data (count=%d)", size)

        // Process all unprocessed finished tasks
        processFinishedTasks(this).let {
            this.clear()
            this.putAll(it)
        }

        // Start new tasks
        processPendingTasks(this).let {
            this.clear()
            this.putAll(it)
        }

        if (size > TASK_HISTORY_LIMIT) {
            Timber.v("Enforcing history limits (%d), need to remove %d.", TASK_HISTORY_LIMIT, size - TASK_HISTORY_LIMIT)
            values
                .filter { it.isFinished }
                .sortedBy { it.finishedAt }
                .take(size - TASK_HISTORY_LIMIT)
                .forEach {
                    Timber.v("Removing from history: %s", get(it.id))
                    remove(it.id)
                }
        }

        Timber.tag(TAG).v(
            "Tasks after processing (count=%d):\n%s",
            size, values.sortedBy { it.finishedAt }.joinToString("\n") {
                it.toLogString()
            }
        )
    }

    private fun processFinishedTasks(data: Map<UUID, InternalTaskState>): Map<UUID, InternalTaskState> {
        val workMap = data.toMutableMap()
        workMap.values
            .filter { it.job.isCompleted && it.executionState != TaskState.ExecutionState.FINISHED }
            .forEach { state ->
                val error = state.job.getCompletionExceptionOrNull()
                val result = if (error == null) {
                    state.job.getCompleted()
                } else {
                    Timber.tag(TAG).e(error, "Task failed: %s", state)
                    val errorHandling = state.request.errorHandling ?: state.config.errorHandling
                    if (errorHandling == TaskFactory.Config.ErrorHandling.ALERT) {
                        error.report(ExceptionCategory.INTERNAL)
                    }
                    error.reportProblem(tag = state.request.type.simpleName)
                    null
                }

                workMap[state.id] = state.copy(
                    finishedAt = timeStamper.nowUTC,
                    result = result,
                    error = error
                )
                Timber.tag(TAG).i("Task is now FINISHED: %s", state)
            }
        return workMap
    }

    private suspend fun processPendingTasks(data: Map<UUID, InternalTaskState>): Map<UUID, InternalTaskState> {
        val workMap = data.toMutableMap()
        workMap.values
            .filter { it.executionState == TaskState.ExecutionState.PENDING }
            .forEach { state ->
                Timber.tag(TAG).d("Checking pending task: %s", state)

                val siblingTasks = workMap.values.filter {
                    it.type == state.type &&
                        it.executionState == TaskState.ExecutionState.RUNNING &&
                        it.id != state.id
                }
                Timber.tag(TAG).d("Task has %d siblings", siblingTasks.size)
                if (siblingTasks.isNotEmpty()) {
                    Timber.tag(TAG).v("Sibling are:\n%s", siblingTasks.joinToString("\n"))
                }

                Timber.tag(TAG).v("Checking preconditions for request: %s", state.config)
                val arePreconditionsMet = state.config.preconditions.fold(true) { allPreConditionsMet, precondition ->
                    allPreConditionsMet && precondition()
                }

                // Handle collision behavior for tasks of same type
                when {
                    !arePreconditionsMet -> {
                        Timber.tag(TAG).d("Preconditions are not met, skipping: %s", state)
                        workMap[state.id] = state.toSkippedState()
                    }
                    siblingTasks.isEmpty() -> {
                        Timber.tag(TAG).d("No siblings exists, running: %s", state)
                        workMap[state.id] = state.toRunningState()
                    }
                    state.config.collisionBehavior == CollisionBehavior.SKIP_IF_SIBLING_RUNNING -> {
                        Timber.tag(TAG).d("Siblings exists, skipping according to collision behavior: %s", state)
                        workMap[state.id] = state.toSkippedState()
                    }
                    state.config.collisionBehavior == CollisionBehavior.ENQUEUE -> {
                        Timber.tag(TAG).d("Postponing task %s", state)
                    }
                }
            }
        return workMap
    }

    private fun InternalTaskState.toRunningState(): InternalTaskState {
        job.invokeOnCompletion {
            Timber.tag(TAG).d("Task ended: %s", this)
            taskScope.launch { processMap() }
        }
        task.progress.onEach {
            Timber.tag(TAG).v("${this.type}(${this.id}) Progress: $it")
        }.launchIn(taskScope)

        job.start()
        return copy(startedAt = timeStamper.nowUTC).also {
            Timber.tag(TAG).i("Starting new task: %s", it)
        }
    }

    private fun InternalTaskState.toSkippedState(): InternalTaskState = copy(
        startedAt = timeStamper.nowUTC,
        finishedAt = timeStamper.nowUTC
    ).also { Timber.tag(TAG).i("Task was skipped: %s", it) }

    private suspend fun <K, V> MutableStateFlow<Map<K, V>>.updateSafely(
        update: suspend MutableMap<K, V>.() -> Unit
    ) = mutex.withLock {
        val mutableMap = value.toMutableMap()
        update(mutableMap)
        value = mutableMap
    }

    /**
     * Don't call this! Only used for testing.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun close() {
        taskQueue.close()
        submissionProcessor.join()
    }

    companion object {
        private const val TAG = "TaskController"
        private const val TASK_HISTORY_LIMIT = 50
    }
}
