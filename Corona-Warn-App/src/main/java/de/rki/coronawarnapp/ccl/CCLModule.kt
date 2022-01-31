package de.rki.coronawarnapp.ccl

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDao
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDatabase
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey
import javax.inject.Singleton

@Module()
abstract class CCLModule {

    @Binds
    @IntoMap
    @TaskTypeKey(DccWalletInfoUpdateTask::class)
    abstract fun dccWalletInfoUpdateTaskFactory(
        factory: DccWalletInfoUpdateTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>

    companion object {
        @Singleton
        @Provides
        fun dccWalletInfoDao(
            dccWalletInfoDatabaseFactory: DccWalletInfoDatabase.Factory
        ): DccWalletInfoDao = dccWalletInfoDatabaseFactory.create().dccWalletInfoDao()
    }
}
