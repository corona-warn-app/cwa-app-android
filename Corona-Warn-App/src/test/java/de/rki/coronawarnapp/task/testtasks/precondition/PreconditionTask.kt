package de.rki.coronawarnapp.task.testtasks.precondition

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Provider

class PreconditionTask : Task<DefaultProgress, PreconditionTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.d("Running with arguments=%s", arguments)

        Result()
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.close()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    class Config(private val arePreconditionsMet: Boolean) : TaskFactory.Config {
        override val executionTimeout: Duration = Duration.standardSeconds(10)

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.ENQUEUE

        override val preconditions: List<suspend () -> Boolean>
            get() = listOf { arePreconditionsMet }
    }

    class Result : Task.Result

    class Factory constructor(
        private val taskByDagger: Provider<PreconditionTask>
    ) : TaskFactory<DefaultProgress, Result> {
        var arePreconditionsMet = true
        override suspend fun createConfig(): TaskFactory.Config = Config(arePreconditionsMet)
        override val taskProvider: () -> Task<DefaultProgress, Result> = { taskByDagger.get() }
    }
}
