package de.rki.coronawarnapp.task.testtasks.timeout

import de.rki.coronawarnapp.task.TaskFactory
import org.joda.time.Duration

class TimeoutTaskConfig : TaskFactory.Config {
    override val executionTimeout: Duration = Duration.standardSeconds(10)

    override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
        TaskFactory.Config.CollisionBehavior.ENQUEUE
}
