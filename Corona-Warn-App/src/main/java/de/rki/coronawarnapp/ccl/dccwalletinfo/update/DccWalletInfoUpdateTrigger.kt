package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask.DccWalletInfoUpdateTriggerType.TriggeredAfterCertificateChange
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask.DccWalletInfoUpdateTriggerType.TriggeredAfterConfigUpdate
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import timber.log.Timber
import javax.inject.Inject

class DccWalletInfoUpdateTrigger @Inject constructor(
    private val taskController: TaskController,
    private val cclSettings: CCLSettings,
    private val appConfigProvider: AppConfigProvider,
) {

    suspend fun triggerDccWalletInfoUpdateAfterConfigUpdate(configurationChanged: Boolean = false) {
        Timber.tag(TAG).d("triggerDccWalletInfoUpdateAfterConfigUpdate()")
        taskController.submit(
            DefaultTaskRequest(
                type = DccWalletInfoUpdateTask::class,
                arguments = DccWalletInfoUpdateTask.Arguments(
                    dccWalletInfoUpdateTriggerType = TriggeredAfterConfigUpdate(
                        configurationChanged
                    ),
                    admissionScenarioId = getAdmissionScenarioId()
                ),
                originTag = TAG
            )
        )
    }

    suspend fun triggerDccWalletInfoUpdateAfterCertificateChange() {
        Timber.tag(TAG).d("triggerDccWalletInfoUpdateAfterCertificateChange()")
        taskController.submit(
            DefaultTaskRequest(
                type = DccWalletInfoUpdateTask::class,
                arguments = DccWalletInfoUpdateTask.Arguments(
                    dccWalletInfoUpdateTriggerType = TriggeredAfterCertificateChange,
                    admissionScenarioId = getAdmissionScenarioId()
                ),
                originTag = TAG
            )
        )
    }

    private suspend fun getAdmissionScenarioId(): String {
        val disabled = runCatching {
            appConfigProvider.getAppConfig().admissionScenariosDisabled
        }.onFailure {
            Timber.d(it, "getAppConfig().admissionScenariosDisabled failed")
        }.getOrElse { true }

        return if (disabled) "" else cclSettings.getAdmissionScenarioId()
    }

    companion object {
        private val TAG = tag<DccWalletInfoUpdateTrigger>()
    }
}
