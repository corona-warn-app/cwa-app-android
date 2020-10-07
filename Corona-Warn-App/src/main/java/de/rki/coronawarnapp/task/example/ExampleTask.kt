package de.rki.coronawarnapp.task.example

import de.rki.coronawarnapp.task.DefaultProgress
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskConfig
import de.rki.coronawarnapp.task.TaskFactory
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject
import javax.inject.Provider

class ExampleTask @Inject constructor() : Task<DefaultProgress> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    override suspend fun run() {
        TODO("Not yet implemented")
    }

    override suspend fun cancel() {
        TODO("Not yet implemented")
    }

    data class Config(
        override val executionMode: TaskConfig.ExecutionMode = TaskConfig.ExecutionMode.ENQUEUE
    ) : TaskConfig

    class Factory @Inject constructor(
        private val taskByDagger: Provider<ExampleTask>,
    ) : TaskFactory<DefaultProgress> {

        override val config: TaskConfig = Config()
        override val taskProvider: () -> Task<DefaultProgress> = { taskByDagger.get() }
    }
}
