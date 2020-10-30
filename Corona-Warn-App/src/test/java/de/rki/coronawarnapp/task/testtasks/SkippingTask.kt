package de.rki.coronawarnapp.task.testtasks

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.task.example.QueueingTask
import org.joda.time.Duration
import javax.inject.Inject
import javax.inject.Provider

class SkippingTask : QueueingTask() {

    class Config : TaskFactory.Config {
        override val executionTimeout: Duration = Duration.standardSeconds(10)

        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING
    }

    class Factory @Inject constructor(
        private val taskByDagger: Provider<QueueingTask>
    ) : TaskFactory<DefaultProgress, Result> {

        override val config: TaskFactory.Config =
            Config()
        override val taskProvider: () -> Task<DefaultProgress, Result> = {
            taskByDagger.get()
        }
    }
}
