package de.rki.coronawarnapp.task

interface TaskFactory<P : Task.Progress> {

    val config: TaskConfig

    val taskProvider: () -> Task<P>
}
