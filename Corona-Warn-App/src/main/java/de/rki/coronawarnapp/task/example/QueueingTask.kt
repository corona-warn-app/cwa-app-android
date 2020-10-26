package de.rki.coronawarnapp.task.example

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.Duration
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Provider

@Suppress("MagicNumber")
open class QueueingTask @Inject constructor() : Task<DefaultProgress, QueueingTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.d("Running with arguments=%s", arguments)
        runSafely(arguments as Arguments).also {
            if (isCanceled) throw TaskCancellationException()
        }
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.close()
    }

    private suspend fun runSafely(arguments: Arguments): Result {
        arguments.path.parentFile!!.mkdirs()

        for (it in arguments.values) {
            if (isCanceled) break
            arguments.path.appendText(it)

            Timber.v("Progress message: $it")
            internalProgress.send(DefaultProgress(it))
            delay(arguments.delay)
        }

        return Result(arguments.path.length())
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    data class Arguments(
        val path: File,
        val values: List<String> = (1..10).map { UUID.randomUUID().toString() },
        val delay: Long = 100L
    ) : Task.Arguments

    data class Result(val writtenBytes: Long) : Task.Result

    class Config : TaskFactory.Config {
        override val executionTimeout: Duration = Duration.standardSeconds(10)

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.ENQUEUE
    }

    class Factory @Inject constructor(
        private val taskByDagger: Provider<QueueingTask>
    ) : TaskFactory<DefaultProgress, Result> {

        override val config: TaskFactory.Config = Config()
        override val taskProvider: () -> Task<DefaultProgress, Result> = { taskByDagger.get() }
    }
}
