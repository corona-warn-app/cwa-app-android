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

        val errorHandling: ErrorHandling
            get() = ErrorHandling.ALERT

        val preconditions: List<suspend () -> Boolean>
            get() = emptyList()

        enum class CollisionBehavior {
            ENQUEUE,
            SKIP_IF_SIBLING_RUNNING
        }

        enum class ErrorHandling {
            SILENT,
            ALERT
        }
    }

    suspend fun createConfig(): Config

    val taskProvider: () -> Task<ProgressType, ResultType>
}
