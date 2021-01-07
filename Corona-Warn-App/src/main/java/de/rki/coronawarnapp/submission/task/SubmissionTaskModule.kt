package de.rki.coronawarnapp.submission.task

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey

@Module
abstract class SubmissionTaskModule {

    @Binds
    @IntoMap
    @TaskTypeKey(SubmissionTask::class)
    abstract fun submissionTaskFactory(
        factory: SubmissionTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>
}
