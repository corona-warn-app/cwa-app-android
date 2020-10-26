package de.rki.coronawarnapp.task.example

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey

@Module
abstract class QueueingTaskModule {

    @Binds
    @IntoMap
    @TaskTypeKey(QueueingTask::class)
    abstract fun exampleTaskFactory(
        factory: QueueingTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>
}
