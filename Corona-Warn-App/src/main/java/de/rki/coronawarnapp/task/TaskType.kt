package de.rki.coronawarnapp.task

enum class TaskType(
    val parallelExecutionPossible: Boolean,
    val parallelExecutionImpossibleSolution: ParallelExecutionImpossibleSolution
) {
    EXAMPLE(true, ParallelExecutionImpossibleSolution.DONT_CARE)
}

enum class ParallelExecutionImpossibleSolution {
    ENQUEUE_MYSELF,
    KILL_OTHERS,
    DONT_CARE
}
