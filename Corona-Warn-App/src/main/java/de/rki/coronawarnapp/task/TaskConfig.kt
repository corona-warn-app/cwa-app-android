package de.rki.coronawarnapp.task

interface TaskConfig {

    val collisionBehavior: CollisionBehavior

    enum class CollisionBehavior {
        ENQUEUE,
        SKIP_IF_ALREADY_RUNNING
    }
}
