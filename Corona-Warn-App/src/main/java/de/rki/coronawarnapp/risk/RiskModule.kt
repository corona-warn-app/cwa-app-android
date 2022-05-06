package de.rki.coronawarnapp.risk

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.risk.storage.DefaultRiskLevelStorage
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey
import de.rki.coronawarnapp.util.reset.Resettable

@Module(includes = [RiskModule.ResetModule::class])
interface RiskModule {

    @Binds
    @IntoMap
    @TaskTypeKey(EwRiskLevelTask::class)
    fun riskLevelTaskFactory(
        factory: EwRiskLevelTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>

    @Binds
    fun bindRiskLevelCalculation(
        riskLevelCalculation: DefaultRiskLevels
    ): RiskLevels

    @Binds
    fun riskLevelStorage(
        storage: DefaultRiskLevelStorage
    ): RiskLevelStorage

    @Module
    interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableRiskLevelStorage(resettable: DefaultRiskLevelStorage): Resettable
    }
}
