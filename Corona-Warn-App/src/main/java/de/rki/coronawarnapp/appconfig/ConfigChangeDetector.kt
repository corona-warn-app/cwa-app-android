package de.rki.coronawarnapp.appconfig

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

    fun start() = appConfigProvider.currentConfig.onEach {
        if (it.identifier != LocalData.lastConfigId()) {
            RiskLevelRepository.setRiskLevelScore(RiskLevel.UNDETERMINED)
            taskController.submit(DefaultTaskRequest(RiskLevelTask::class))
            LocalData.lastConfigId(it.identifier)
        }
    }
}
