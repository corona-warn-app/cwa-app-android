package de.rki.coronawarnapp.task.testtasks.silenterror

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.Finished
import de.rki.coronawarnapp.task.common.Started
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Provider

class SilentErrorTask : Task<DefaultProgress, SilentErrorTask.Result> {

    private val internalProgress = MutableStateFlow<DefaultProgress>(Started)
    override val progress: Flow<DefaultProgress> = internalProgress

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.d("Running with arguments=%s", arguments)
        arguments as Arguments
        throw arguments.error
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.value = Finished
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    data class Arguments(
        val error: Throwable
    ) : Task.Arguments

    class Result : Task.Result

    class Factory constructor(
        private val taskByDagger: Provider<SilentErrorTask>
    ) : TaskFactory<DefaultProgress, Result> {
        override suspend fun createConfig(): TaskFactory.Config = SilentErrorTaskConfig()
        override val taskProvider: () -> Task<DefaultProgress, Result> = { taskByDagger.get() }
    }
}
