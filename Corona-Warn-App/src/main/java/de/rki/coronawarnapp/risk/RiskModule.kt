package de.rki.coronawarnapp.risk

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.risk.storage.DefaultRiskLevelStorage
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey
import javax.inject.Singleton

@Module
class RiskModule {

    @Provides
    @IntoMap
    @TaskTypeKey(RiskLevelTask::class)
    fun riskLevelTaskFactory(
        factory: RiskLevelTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result> = factory

    @Provides
    @Singleton
    fun bindRiskLevelCalculation(
        riskLevelCalculation: DefaultRiskLevels
    ): RiskLevels = riskLevelCalculation

    @Provides
    @Singleton
    fun riskLevelStorage(
        storage: DefaultRiskLevelStorage
    ): RiskLevelStorage = storage
}
