package de.rki.coronawarnapp.task

interface TaskConfig {

    val executionMode: ExecutionMode

    enum class ExecutionMode {
        ENQUEUE,
        REPLACE,
        PARALLEL
    }
}
