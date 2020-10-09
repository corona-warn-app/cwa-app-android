package de.rki.coronawarnapp.task.example

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.lang.Thread.sleep
import javax.inject.Inject
import javax.inject.Provider

class ExampleTask @Inject constructor() : Task<DefaultProgress, ExampleResult> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    override suspend fun run(arguments: Task.Arguments): ExampleResult {
        (1..10).forEach {
            internalProgress.send(DefaultProgress("$arguments-$it"))
            sleep(1000L)
        }
        TODO("Not yet implemented")
    }

    override suspend fun cancel() {
        TODO("Not yet implemented")
    }

    data class Config(
        override val collisionBehavior: TaskFactory.Config.CollisionBehavior = TaskFactory.Config.CollisionBehavior.ENQUEUE
    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<ExampleTask>,
    ) : TaskFactory<DefaultProgress, ExampleResult> {

        override val config: TaskFactory.Config = Config()
        override val taskProvider: () -> Task<DefaultProgress, ExampleResult> = {
            taskByDagger.get()
        }
    }
}
