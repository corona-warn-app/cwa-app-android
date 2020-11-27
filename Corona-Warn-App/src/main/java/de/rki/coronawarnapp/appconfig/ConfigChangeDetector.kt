package de.rki.coronawarnapp.appconfig

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
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
    private val riskLevelSettings: RiskLevelSettings,
    private val riskLevelStorage: RiskLevelStorage
) {

    fun launch() {
        Timber.tag(TAG).v("Monitoring config changes.")
        appConfigProvider.currentConfig
            .distinctUntilChangedBy { it.identifier }
            .onEach {
                Timber.tag(TAG).v("Running app config change checks.")
                check(it.identifier)
            }
            .catch { Timber.tag(TAG).e(it, "App config change checks failed.") }
            .launchIn(appScope)
    }

    @VisibleForTesting
    internal suspend fun check(newIdentifier: String) {
        if (riskLevelSettings.lastUsedConfigIdentifier == null) {
            // No need to reset anything if we didn't calculate a risklevel yet.
            Timber.tag(TAG).d("Config changed, but no previous identifier is available.")
            return
        }

        val oldConfigId = riskLevelSettings.lastUsedConfigIdentifier
        if (newIdentifier != oldConfigId) {
            Timber.tag(TAG).i("New config id ($newIdentifier) differs from last one ($oldConfigId), resetting.")
            riskLevelStorage.clear()
            taskController.submit(DefaultTaskRequest(RiskLevelTask::class, originTag = "ConfigChangeDetector"))
        } else {
            Timber.tag(TAG).v("Config identifier ($oldConfigId) didn't change, NOOP.")
        }
    }

    companion object {
        private const val TAG = "ConfigChangeDetector"
    }
}
