package de.rki.coronawarnapp.task

import de.rki.coronawarnapp.task.ParallelExecutionImpossibleSolution.DONT_CARE
import de.rki.coronawarnapp.task.ParallelExecutionImpossibleSolution.ENQUEUE_MYSELF
import de.rki.coronawarnapp.task.ParallelExecutionImpossibleSolution.KILL_OTHERS
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject

class TaskController /* TODO better name*/ @Inject constructor(
    private val stateFactory: TaskStateFactory
) {

    private val mutex = Mutex()

    private val queues = mutableMapOf<TaskType, Queue<Task<out TaskProgress>>>()

    private val executing = mutableMapOf<TaskState, Task<out TaskProgress>>()

    private val listeners = mutableListOf<TaskListener>()

    fun addListener(listener: TaskListener) = listeners.add(listener)

    suspend fun <P : TaskProgress> start(task: Task<P>, type: TaskType) {
        mutex.withLock {
            if (type.parallelExecutionPossible) {
                execute(task, type)
            } else {
                val queue =
                    queues[type] ?: LinkedList<Task<out TaskProgress>>().also {
                        queues[type] = it
                    }
                val alreadyExecutingTask = executing.keys.find { it.type == type }
                val queueHasItems = !queue.isEmpty()
                val alreadyExecuting = alreadyExecutingTask != null
                if (alreadyExecuting || queueHasItems) {
                    when (type.parallelExecutionImpossibleSolution) {
                        KILL_OTHERS -> {
                            if (alreadyExecutingTask != null){
                                executing[alreadyExecutingTask]?.cancel()
                            }
                            queue.clear()
                        }
                        ENQUEUE_MYSELF, DONT_CARE -> {
                            queue.add(task)
                        }
                    }
                }
                else{
                    execute(task, type)
                }
            }
        }
    }

    private fun <P : TaskProgress> execute(task: Task<P>, type: TaskType) {
        stateFactory.createState(task, type).also {
            executing[it] = task
            fireStateChanged(it)
        }
    }

    private fun fireStateChanged(state: TaskState) =
        checkListenersAndFire { it.onStateChanged(state) }

    private fun fireError(state: TaskState) =
        checkListenersAndFire { it.onError(state) }

    private fun fireProgress(state: TaskState, progress: TaskProgress) =
        checkListenersAndFire { it.onProgress(state, progress) }

    private fun checkListenersAndFire(event: (TaskListener) -> Unit) {
        val deadListeners = mutableListOf<TaskListener>()
        listeners.forEach {
            if (!it.isAlive) {
                deadListeners.add(it)
            } else {
                event.invoke(it)
            }
        }
        listeners.removeAll(deadListeners)
    }
}
