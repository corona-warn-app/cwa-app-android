package de.rki.coronawarnapp.test.tasks.testtask

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class TestTask @Inject constructor() : Task<DefaultProgress, TestTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.d("Running with arguments=%s", arguments)
        arguments as Arguments
        runSafely(arguments).also {
            if (isCanceled) throw TaskCancellationException()
        }
    } finally {
        internalProgress.close()
    }

    @Suppress("MagicNumber")
    private suspend fun runSafely(arguments: Arguments): Result {
        for (it in 1..10) {
            internalProgress.send(DefaultProgress("${arguments.prefix}: ${Instant.now()}"))

            delay(1000)

            if (isCanceled) break
        }
        return Result()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    class Arguments(
        val prefix: String
    ) : Task.Arguments

    class Result : Task.Result

    class Config : TaskFactory.Config {
        override val executionTimeout: Duration = Duration.standardSeconds(10)

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.ENQUEUE
    }

    class Factory @Inject constructor(
        private val taskByDagger: Provider<TestTask>
    ) : TaskFactory<DefaultProgress, Result> {

        override val config: TaskFactory.Config = Config()
        override val taskProvider: () -> Task<DefaultProgress, Result> = {
            taskByDagger.get()
        }
    }
}
