package de.rki.coronawarnapp.task.testtasks.alerterror

import de.rki.coronawarnapp.task.TaskFactory
import org.joda.time.Duration

class AlertErrorTaskConfig : TaskFactory.Config {
    override val executionTimeout: Duration = Duration.standardSeconds(10)

    override val errorHandling: TaskFactory.Config.ErrorHandling = TaskFactory.Config.ErrorHandling.ALERT

    override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
        TaskFactory.Config.CollisionBehavior.ENQUEUE
}
