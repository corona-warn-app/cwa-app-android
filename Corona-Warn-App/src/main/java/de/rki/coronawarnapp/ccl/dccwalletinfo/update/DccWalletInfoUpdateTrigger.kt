package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask.DccWalletInfoUpdateTriggerType.TriggeredAfterCertificateChange
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask.DccWalletInfoUpdateTriggerType.TriggeredAfterConfigUpdate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccWalletInfoUpdateTrigger @Inject constructor(
    private val taskController: TaskController,
    private val cclSettings: CCLSettings,
    private val appConfigProvider: AppConfigProvider,
    certificateProvider: CertificateProvider,
    @AppScope appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider
) {

    init {
        certificateProvider.certificateContainer
            .onEach {
                Timber.tag(TAG).e("Certificates changed!")
                triggerDccWalletInfoUpdateAfterCertificateChange()
            }
            .launchIn(scope = appScope + dispatcherProvider.IO)
    }

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
        val enabled = runCatching {
            appConfigProvider.getAppConfig().admissionScenariosEnabled
        }.onFailure {
            Timber.d(it, "getAppConfig().admissionScenariosEnabled failed")
        }.getOrElse { true }

        return if (enabled) cclSettings.getAdmissionScenarioId() else ""
    }

    companion object {
        private val TAG = tag<DccWalletInfoUpdateTrigger>()
    }
}
