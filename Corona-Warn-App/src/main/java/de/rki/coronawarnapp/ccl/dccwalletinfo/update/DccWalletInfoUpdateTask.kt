package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import de.rki.coronawarnapp.ccl.dccwalletinfo.DccWalletInfoCleaner
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskFactory.Config.CollisionBehavior
import de.rki.coronawarnapp.task.TaskFactory.Config.ErrorHandling
import de.rki.coronawarnapp.task.common.DefaultProgress
import de.rki.coronawarnapp.task.common.Started
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.joda.time.Duration
import javax.inject.Inject
import javax.inject.Provider

class DccWalletInfoUpdateTask @Inject constructor(
    private val dccWalletInfoCalculationManager: DccWalletInfoCalculationManager,
    private val dccWalletInfoCleaner: DccWalletInfoCleaner,
) : Task<DefaultProgress, Task.Result> {
    private val taskProgress = MutableStateFlow<DefaultProgress>(Started)
    override val progress: Flow<DefaultProgress> = taskProgress

    override suspend fun run(arguments: Task.Arguments): Task.Result {
        arguments as Arguments
        delay(arguments.startDelay) // To capture latest data before calculation
        dccWalletInfoCalculationManager.triggerCalculation(arguments.configurationChanged)
        dccWalletInfoCleaner.clean()
        return object : Task.Result {}
    }

    override suspend fun cancel() = Unit

    class Config : TaskFactory.Config {
        override val executionTimeout: Duration = Duration.standardMinutes(9)
        override val collisionBehavior: CollisionBehavior = CollisionBehavior.ENQUEUE
        override val errorHandling: ErrorHandling = ErrorHandling.SILENT
    }

    class Factory @Inject constructor(
        private val taskByDagger: Provider<DccWalletInfoUpdateTask>
    ) : TaskFactory<DefaultProgress, Task.Result> {

        override suspend fun createConfig(): TaskFactory.Config = Config()

        override val taskProvider: () -> Task<DefaultProgress, Task.Result> = { taskByDagger.get() }
    }

    data class Arguments(
        val startDelay: Long = 1_000L,
        val configurationChanged: Boolean = true
    ) : Task.Arguments
}
