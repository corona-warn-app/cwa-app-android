package de.rki.coronawarnapp.task

interface TaskFactory<
    ProgressType : Task.Progress,
    ResultType : Task.Result
    > {

    val config: TaskConfig

    val taskProvider: () -> Task<ProgressType, ResultType>
}
