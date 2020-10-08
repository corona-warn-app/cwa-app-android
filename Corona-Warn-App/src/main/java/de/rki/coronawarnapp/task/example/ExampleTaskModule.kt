package de.rki.coronawarnapp.task.example

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskType
import de.rki.coronawarnapp.task.TaskTypeKey

@Module
abstract class ExampleTaskModule {

    @Binds
    @IntoMap
    @TaskTypeKey(TaskType.EXAMPLE)
    abstract fun exampleTaskFactory(
        factory: ExampleTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>
}
