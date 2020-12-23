package de.rki.coronawarnapp.task.testtasks.silenterror

import de.rki.coronawarnapp.task.TaskFactory
import org.joda.time.Duration

class SilentErrorTaskConfig : TaskFactory.Config {
    override val executionTimeout: Duration = Duration.standardSeconds(10)

    override val errorHandling: TaskFactory.Config.ErrorHandling = TaskFactory.Config.ErrorHandling.SILENT

    override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
        TaskFactory.Config.CollisionBehavior.ENQUEUE
}
