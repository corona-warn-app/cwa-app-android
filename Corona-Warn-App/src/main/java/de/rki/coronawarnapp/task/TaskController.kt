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
            .onEach {
                Timber.tag(TAG).d("Processing new task request: %s", it)
                initTaskData(it)
            }
            .launchIn(taskScope)
    }

    fun submitTask(request: TaskRequest) {
        Timber.tag(TAG).i("Task submitted: %s", request)
        taskQueue.offer(request)
    }

    private suspend fun initTaskData(request: TaskRequest) {
        val taskFactory = taskFactories[request::class.java]
        requireNotNull(taskFactory) { "No factory available for $request" }
        Timber.tag(TAG).v("Initiating task data for request: %s", request)

        val taskConfig = taskFactory.config
        val task = taskFactory.taskProvider()

        val deferred = taskScope.async(
            start = CoroutineStart.LAZY
        ) { task.run(request.arguments) }

        val activeTask = InternalTaskData(
            request = request,
            createdAt = timeStamper.nowUTC,
            config = taskConfig,
            task = task,
            deferred = deferred
        )

        internalTaskData.updateSafely {
            put(activeTask.id, activeTask)
            Timber.tag(TAG).d("New task data created: %s", activeTask)
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
