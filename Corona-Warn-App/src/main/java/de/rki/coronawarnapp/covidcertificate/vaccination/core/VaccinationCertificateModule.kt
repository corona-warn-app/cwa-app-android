package de.rki.coronawarnapp.covidcertificate.vaccination.core

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.covidcertificate.vaccination.core.execution.task.VaccinationUpdateTask
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey

@Module
abstract class VaccinationCertificateModule {
    @Binds
    @IntoMap
    @TaskTypeKey(VaccinationUpdateTask::class)
    abstract fun vaccinationUpdateTaskFactory(
        factory: VaccinationUpdateTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>
}
