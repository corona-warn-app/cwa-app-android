package de.rki.coronawarnapp.appconfig

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ConfigChangeDetector @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    private val taskController: TaskController
) {

    fun start() = appConfigProvider.currentConfig.onEach { check(it.identifier) }

    @VisibleForTesting
    internal fun check(identifier: String) {
        if (identifier != LocalData.lastConfigId()) {
            RiskLevelRepositoryDeferrer.resetRiskLevel()
            taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
            LocalData.lastConfigId(identifier)
        }
    }

    @VisibleForTesting
    internal object RiskLevelRepositoryDeferrer {

        fun resetRiskLevel() {
            RiskLevelRepository.setRiskLevelScore(RiskLevel.UNDETERMINED)
        }
    }
}
