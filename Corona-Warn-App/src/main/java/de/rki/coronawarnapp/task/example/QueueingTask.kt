package de.rki.coronawarnapp.task.example

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancelationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Provider

open class QueueingTask @Inject constructor() : Task<DefaultProgress, QueueingTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result {
        Timber.d("Running with arguments=%s", arguments)
        arguments as Arguments

        arguments.path.parentFile!!.mkdirs()

        for (it in arguments.values) {
            if (isCanceled) break
            arguments.path.appendText(it)

            Timber.v("Progress message: $it")
            internalProgress.send(DefaultProgress(it))
            delay(arguments.delay)
        }

        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.close()

        if (isCanceled) {
            throw TaskCancelationException()
        } else {
            return Result(arguments.path.length())
        }
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    @Suppress("MagicNumber")
    data class Arguments(
        val path: File,
        val values: List<String> = (1..10).map { UUID.randomUUID().toString() },
        val delay: Long = 100L
    ) : Task.Arguments

    data class Result(
        val writtenBytes: Long
    ) : Task.Result

    data class Config(
        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.ENQUEUE

    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<QueueingTask>
    ) : TaskFactory<DefaultProgress, Result> {

        override val config: TaskFactory.Config = Config()
        override val taskProvider: () -> Task<DefaultProgress, Result> = {
            taskByDagger.get()
        }
    }
}
