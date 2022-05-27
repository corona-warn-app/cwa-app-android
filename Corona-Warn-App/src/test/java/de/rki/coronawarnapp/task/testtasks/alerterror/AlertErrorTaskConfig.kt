package de.rki.coronawarnapp.task.testtasks.alerterror

import de.rki.coronawarnapp.task.TaskFactory
import java.time.Duration

class AlertErrorTaskConfig : TaskFactory.Config {
    override val executionTimeout: Duration = Duration.ofSeconds(10)

    override val errorHandling: TaskFactory.Config.ErrorHandling = TaskFactory.Config.ErrorHandling.ALERT

    override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
        TaskFactory.Config.CollisionBehavior.ENQUEUE
}
