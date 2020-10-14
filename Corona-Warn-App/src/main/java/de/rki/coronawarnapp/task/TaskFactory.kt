package de.rki.coronawarnapp.task

import org.joda.time.Duration

interface TaskFactory<
    ProgressType : Task.Progress,
    ResultType : Task.Result
    > {

    interface Config {
        /**
         * The maximal runtime of the task, before it is canceled by the controller
         */
        val executionTimeout: Duration

        val collisionBehavior: CollisionBehavior

        enum class CollisionBehavior {
            ENQUEUE,
            SKIP_IF_SIBLING_RUNNING
        }
    }

    val config: Config

    val taskProvider: () -> Task<ProgressType, ResultType>
}
