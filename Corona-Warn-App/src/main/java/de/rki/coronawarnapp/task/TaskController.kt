package de.rki.coronawarnapp.task

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskController @Inject constructor(
    private val taskFactories: @JvmSuppressWildcards Map<TaskType, TaskFactory<out Task.Progress>>
) {

    init {
        Timber.d("We have factories for %s", taskFactories.keys)
    }

//    private val mutex = Mutex()
//
//    private val queues = mutableMapOf<TaskType, Queue<Task<out TaskProgress>>>()
//
//    private val executing = mutableMapOf<ActiveTask, Task<out TaskProgress>>()
//
//    val activeTasks: Flow<Map<ActiveTask, Task.Progress>> = internalActiveTasks.asFlow()
//
//    private val internalActiveTasks = ConflatedBroadcastChannel<Map<ActiveTask, Task.Progress>>()
//
//    suspend fun start(type: TaskType) {
//
//        activeTasks
//            .map {
//                it.keys.filter { it.type == TaskType.EXAMPLE  }
//            }
//        mutex.withLock {
//            if (type.parallelExecutionPossible) {
//                execute(task, type)
//            } else {
//                val queue =
//                    queues[type] ?: LinkedList<Task<out TaskProgress>>().also {
//                        queues[type] = it
//                    }
//                val alreadyExecutingTask = executing.keys.find { it.type == type }
//                val queueHasItems = !queue.isEmpty()
//                val alreadyExecuting = alreadyExecutingTask != null
//                if (alreadyExecuting || queueHasItems) {
//                    when (type.parallelExecutionImpossibleSolution) {
//                        KILL_OTHERS -> {
//                            if (alreadyExecutingTask != null) {
//                                executing[alreadyExecutingTask]?.cancel()
//                            }
//                            queue.clear()
//                        }
//                        ENQUEUE_MYSELF, DONT_CARE -> {
//                            queue.add(task)
//                        }
//                    }
//                } else {
//                    execute(task, type)
//                }
//            }
//        }
//    }
//
//    private fun <P : TaskProgress> execute(task: Task<P>, type: TaskType) {
//        stateFactory.createState(task, type).also {
//            executing[it] = task
//            fireStateChanged(it)
//        }
//    }
}
