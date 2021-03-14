package de.rki.coronawarnapp.test.tasks

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey
import de.rki.coronawarnapp.test.tasks.testtask.TestTask

@Module
abstract class TaskControllerTestModule {

    @Binds
    @IntoMap
    @TaskTypeKey(TestTask::class)
    abstract fun testTaskFactory(
        factory: TestTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>
}
