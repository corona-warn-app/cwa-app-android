package de.rki.coronawarnapp.risk

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey
import javax.inject.Singleton

@Module
abstract class RiskModule {

    @Binds
    @IntoMap
    @TaskTypeKey(RiskLevelTask::class)
    abstract fun riskLevelTaskFactory(
        factory: RiskLevelTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>

    @Binds
    @Singleton
    abstract fun bindRiskLevelCalculation(
        riskLevelCalculation: DefaultRiskLevels
    ): RiskLevels
}
