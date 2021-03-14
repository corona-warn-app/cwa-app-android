package de.rki.coronawarnapp.task.testtasks.alerterror

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import timber.log.Timber
import javax.inject.Provider

class AlertErrorTask : Task<DefaultProgress, AlertErrorTask.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments): Result = try {
        Timber.d("Running with arguments=%s", arguments)
        arguments as Arguments
        throw arguments.error
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.close()
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
        private val taskByDagger: Provider<AlertErrorTask>
    ) : TaskFactory<DefaultProgress, Result> {
        override suspend fun createConfig(): TaskFactory.Config = AlertErrorTaskConfig()
        override val taskProvider: () -> Task<DefaultProgress, Result> = { taskByDagger.get() }
    }
}
