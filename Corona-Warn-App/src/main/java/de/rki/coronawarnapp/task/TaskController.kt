package de.rki.coronawarnapp.task

import de.rki.coronawarnapp.task.TaskFactory.Config.CollisionBehavior
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
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskController @Inject constructor(
    private val taskFactories: Map<Class<out TaskRequest>, @JvmSuppressWildcards TaskFactory<out Task.Progress, out Task.Result>>,
    @TaskCoroutineScope private val taskScope: CoroutineScope,
    private val timeStamper: TimeStamper
) {

    private val mutex = Mutex()
    private val taskQueue = Channel<TaskRequest>(Channel.UNLIMITED)
    private val internalTaskData = MutableStateFlow<Map<UUID, InternalTaskData>>(
        emptyMap()
    )
    val tasks: Flow<Map<TaskData, Flow<Task.Progress>>> = internalTaskData
        .map { it.values }
        .map { tasks ->
            tasks.map { it to it.task.progress }.toMap()
        }

    init {
        Timber.tag(TAG).d("We have factories for %s", taskFactories.keys)

        taskQueue
            .receiveAsFlow()
            .onStart { Timber.tag(TAG).v("Listening to task request queue.") }
            .onCompletion { Timber.tag(TAG).w("Stopped listening to request queue. Why?") }
            .onEach { initTaskData(it) }
            .launchIn(taskScope)
    }

    /**
     * Don't re-use taskrequests, create new requests for each submission.
     * They contain unique IDs.
     */
    fun submitTask(request: TaskRequest) {
        if (!taskFactories.containsKey(request::class.java)) {
            throw MissingTaskFactory(request::class)
        }
        Timber.tag(TAG).i("Task submitted: %s", request)
        taskQueue.offer(request)
    }

    private suspend fun initTaskData(newRequest: TaskRequest) {
        Timber.tag(TAG).d("Processing new task request: %s", newRequest)
        val taskFactory = taskFactories[newRequest::class.java]
        requireNotNull(taskFactory) { "No factory available for $newRequest" }

        Timber.tag(TAG).v("Initiating task data for request: %s", newRequest)
        val taskConfig = taskFactory.config
        val task = taskFactory.taskProvider()

        val deferred = taskScope.async(
            start = CoroutineStart.LAZY
        ) { task.run(newRequest.arguments) }

        val activeTask = InternalTaskData(
            request = newRequest,
            createdAt = timeStamper.nowUTC,
            config = taskConfig,
            task = task,
            deferred = deferred
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
        Timber.tag(TAG).v("Tasks before processing: %s", this.values)

        // Procress all unprocessed finished tasks
        values.toList()
            .filter { it.deferred.isCompleted && it.state != TaskData.State.FINISHED }
            .forEach { data ->
                val error = data.deferred.getCompletionExceptionOrNull()
                val result = if (error == null) {
                    data.deferred.getCompleted()
                } else {
                    Timber.tag(TAG).e(error, "Task failed: %s", data)
                    null
                }

                this[data.id] = data.copy(
                    completedAt = timeStamper.nowUTC,
                    result = result,
                    error = error
                )
                Timber.tag(TAG).i("Task is now FINISHED: %s", data)
            }

        // Start new tasks
        values.toList()
            .filter { it.state == TaskData.State.PENDING }
            .forEach { data ->
                Timber.tag(TAG).d("Checking pending task: %s", data)

                val siblingTasks = values.filter {
                    it.type == data.type
                        && it.state == TaskData.State.RUNNING
                        && it.id != data.id
                }
                Timber.tag(TAG).d("Task has %d siblings", siblingTasks.size)
                Timber.tag(TAG).v(
                    "Sibling are:\n%s", siblingTasks.joinToString("\n")
                )

                // Handle collision behavior for tasks of same type
                when {
                    siblingTasks.isEmpty() -> {
                        this[data.id] = data.toRunningState()
                    }
                    data.config.collisionBehavior == CollisionBehavior.SKIP_IF_ALREADY_RUNNING -> {

                    }
                    data.config.collisionBehavior == CollisionBehavior.ENQUEUE -> {
                        Timber.tag(TAG).d("Postponing task %s", data)
                    }
                }
            }

        Timber.tag(TAG).v("Tasks after processing: %s", this.values)
    }

    private fun InternalTaskData.toRunningState(): InternalTaskData {
        deferred.invokeOnCompletion {
            Timber.tag(TAG).d("Task ended (type=%s, id=%s)", type, id)
            taskScope.launch { processMap() }
        }
        deferred.start()
        return copy(startedAt = timeStamper.nowUTC).also {
            Timber.tag(TAG).i("Starting new task: %s", it)
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
