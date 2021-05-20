package de.rki.coronawarnapp.task.testtasks.timeout

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskCancellationException
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.Finished
import de.rki.coronawarnapp.task.common.Started
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Provider

abstract class BaseTimeoutTask : Task<DefaultProgress, TimeoutTaskResult> {

    private val internalProgress = MutableStateFlow<DefaultProgress>(Started)
    override val progress: Flow<DefaultProgress> = internalProgress

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): TimeoutTaskResult = try {
        Timber.d("Running with arguments=%s", arguments)
        runSafely(arguments as TimeoutTaskArguments).also {
            if (isCanceled) throw TaskCancellationException()
        }
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.value = Finished
    }

    private suspend fun runSafely(arguments: TimeoutTaskArguments): TimeoutTaskResult {
        delay(arguments.delay)
        return TimeoutTaskResult()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    abstract class Factory constructor(
        private val taskByDagger: Provider<BaseTimeoutTask>
    ) : TaskFactory<DefaultProgress, TimeoutTaskResult> {

        override suspend fun createConfig(): TaskFactory.Config = TimeoutTaskConfig()
        override val taskProvider: () -> Task<DefaultProgress, TimeoutTaskResult> =
            { taskByDagger.get() }
    }
}
