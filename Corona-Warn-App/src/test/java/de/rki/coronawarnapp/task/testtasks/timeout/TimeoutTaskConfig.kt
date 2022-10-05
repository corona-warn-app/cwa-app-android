package de.rki.coronawarnapp.task.testtasks.timeout

import de.rki.coronawarnapp.task.TaskFactory
import java.time.Duration

class TimeoutTaskConfig : TaskFactory.Config {
    override val executionTimeout: Duration = Duration.ofSeconds(10)

    override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
        TaskFactory.Config.CollisionBehavior.ENQUEUE
}
