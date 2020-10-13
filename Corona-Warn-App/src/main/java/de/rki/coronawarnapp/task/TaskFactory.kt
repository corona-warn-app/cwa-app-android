package de.rki.coronawarnapp.task

interface TaskFactory<
    ProgressType : Task.Progress,
    ResultType : Task.Result
    > {

    interface Config {
        /**
         * the maximal runtime of the task
         * in milliseconds
         */
        val timeout: Long?

        val collisionBehavior: CollisionBehavior

        enum class CollisionBehavior {
            ENQUEUE,
            SKIP_IF_SIBLING_RUNNING
        }
    }

    val config: Config

    val taskProvider: () -> Task<ProgressType, ResultType>
}
