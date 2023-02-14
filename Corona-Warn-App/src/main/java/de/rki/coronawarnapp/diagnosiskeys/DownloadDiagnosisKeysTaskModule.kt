package de.rki.coronawarnapp.diagnosiskeys

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysTask
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey

@InstallIn(SingletonComponent::class)
@Module
abstract class DownloadDiagnosisKeysTaskModule {

    @Binds
    @IntoMap
    @TaskTypeKey(DownloadDiagnosisKeysTask::class)
    abstract fun downloadDiagnosisKeysTaskFactory(
        factory: DownloadDiagnosisKeysTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>
}
