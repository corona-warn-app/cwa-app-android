package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.ccl.dccadmission.model.Scenario
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask.DccWalletInfoUpdateTriggerType.TriggeredAfterCertificateChange
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask.DccWalletInfoUpdateTriggerType.TriggeredAfterConfigUpdate
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import timber.log.Timber
import javax.inject.Inject

class DccWalletInfoUpdateTrigger @Inject constructor(
    private val taskController: TaskController,
    private val cclSettings: CclSettings,
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
                    admissionScenarioId = admissionScenarioId()
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
                    admissionScenarioId = admissionScenarioId()
                ),
                originTag = TAG
            )
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun admissionScenarioId(): String =
        if (appConfigProvider.getAppConfig().admissionScenariosEnabled) {
            cclSettings.getAdmissionScenarioId()
        } else {
            Timber.tag(TAG).d(
                "admissionScenarios feature is disabled, `scenarioIdentifier` is replaced by ${Scenario.DEFAULT_ID} "
            )
            Scenario.DEFAULT_ID
        }

    companion object {
        private val TAG = tag<DccWalletInfoUpdateTrigger>()
    }
}
