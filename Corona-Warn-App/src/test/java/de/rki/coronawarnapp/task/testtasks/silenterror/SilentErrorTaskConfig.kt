package de.rki.coronawarnapp.task.testtasks.silenterror

import de.rki.coronawarnapp.task.TaskFactory
import java.time.Duration

class SilentErrorTaskConfig : TaskFactory.Config {
    override val executionTimeout: Duration = Duration.ofSeconds(10)

    override val errorHandling: TaskFactory.Config.ErrorHandling = TaskFactory.Config.ErrorHandling.SILENT

    override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
        TaskFactory.Config.CollisionBehavior.ENQUEUE
}
