package de.rki.coronawarnapp.task.example

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import javax.inject.Inject
import javax.inject.Provider

class SkippingTask : QueueingTask() {

    data class Config(
        override val collisionBehavior: TaskFactory.Config.CollisionBehavior = TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING
    ) : TaskFactory.Config

    class Factory @Inject constructor(
        private val taskByDagger: Provider<QueueingTask>,
    ) : TaskFactory<DefaultProgress, Result> {

        override val config: TaskFactory.Config = Config()
        override val taskProvider: () -> Task<DefaultProgress, Result> = {
            taskByDagger.get()
        }
    }
}
