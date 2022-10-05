package de.rki.coronawarnapp.test.tasks.testtask

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.Finished
import de.rki.coronawarnapp.task.common.ProgressResult
import de.rki.coronawarnapp.task.common.Started
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Provider

class TestTask @Inject constructor() : Task<DefaultProgress, TestTask.Result> {

    private val internalProgress = MutableStateFlow<DefaultProgress>(Started)
    override val progress: Flow<DefaultProgress> = internalProgress

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.d("Running with arguments=%s", arguments)
        arguments as Arguments
        runSafely(arguments).also {
            if (isCanceled) throw TaskCancellationException()
        }
    } finally {
        internalProgress.value = Finished
    }

    private suspend fun runSafely(arguments: Arguments): Result {
        for (it in 1..10) {
            internalProgress.value = ProgressResult("${arguments.prefix}: ${Instant.now()}")
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
        override val executionTimeout: Duration = Duration.ofSeconds(10)

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.ENQUEUE
    }

    class Factory @Inject constructor(
        private val taskByDagger: Provider<TestTask>
    ) : TaskFactory<DefaultProgress, Result> {

        override suspend fun createConfig(): TaskFactory.Config = Config()

        override val taskProvider: () -> Task<DefaultProgress, Result> = {
            taskByDagger.get()
        }
    }
}
