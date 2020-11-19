package de.rki.coronawarnapp.appconfig

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelData
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class ConfigChangeDetector @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    private val taskController: TaskController,
    @AppScope private val appScope: CoroutineScope,
    private val riskLevelData: RiskLevelData
) {

    fun launch() {
        Timber.v("Monitoring config changes.")
        appConfigProvider.currentConfig
            .distinctUntilChangedBy { it.identifier }
            .onEach {
                Timber.v("Running app config change checks.")
                check(it.identifier)
            }
            .catch { Timber.e(it, "App config change checks failed.") }
            .launchIn(appScope)
    }

    @VisibleForTesting
    internal fun check(newIdentifier: String) {
        if (riskLevelData.lastUsedConfigIdentifier == null) {
            // No need to reset anything if we didn't calculate a risklevel yet.
            Timber.d("Config changed, but no previous identifier is available.")
            return
        }

        val oldConfigId = riskLevelData.lastUsedConfigIdentifier
        if (newIdentifier != oldConfigId) {
            Timber.i("New config id ($newIdentifier) differs from last one ($oldConfigId), resetting.")
            RiskLevelRepositoryDeferrer.resetRiskLevel()
            taskController.submit(DefaultTaskRequest(RiskLevelTask::class, originTag = "ConfigChangeDetector"))
        } else {
            Timber.v("Config identifier ($oldConfigId) didn't change, NOOP.")
        }
    }

    @VisibleForTesting
    internal object RiskLevelRepositoryDeferrer {

        fun resetRiskLevel() {
            RiskLevelRepository.setRiskLevelScore(RiskLevel.UNDETERMINED)
        }
    }
}
