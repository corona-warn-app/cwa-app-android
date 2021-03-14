package de.rki.coronawarnapp.contactdiary.retention

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey

@Module
abstract class ContactDiaryRetentionModule {

    @Binds
    @IntoMap
    @TaskTypeKey(ContactDiaryCleanTask::class)
    abstract fun contactDiaryCleanTaskFactory(
        factory: ContactDiaryCleanTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>
}
