package de.rki.coronawarnapp.ccl

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey

@Module
abstract class CCLModule {

    @Binds
    @IntoMap
    @TaskTypeKey(DccWalletInfoUpdateTask::class)
    abstract fun dccWalletInfoUpdateTaskFactory(
        factory: DccWalletInfoUpdateTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>
}
