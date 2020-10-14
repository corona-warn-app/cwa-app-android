package de.rki.coronawarnapp.task

interface TaskFactory<
    ProgressType : Task.Progress,
    ResultType : Task.Result
    > {

    interface Config {
        val collisionBehavior: CollisionBehavior

        enum class CollisionBehavior {
            ENQUEUE,
            SKIP_IF_SIBLING_RUNNING
        }
    }

    val config: Config

    val taskProvider: () -> Task<ProgressType, ResultType>
}
