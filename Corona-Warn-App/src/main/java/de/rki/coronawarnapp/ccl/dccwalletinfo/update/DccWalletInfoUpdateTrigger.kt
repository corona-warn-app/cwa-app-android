package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.ccl.dccadmission.model.Scenario
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccwalletinfo.DccWalletInfoCleaner
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccWalletInfoUpdateTrigger @Inject constructor(
    private val taskController: TaskController,
    private val cclSettings: CclSettings,
    private val appConfigProvider: AppConfigProvider,
    personCertificateProvider: PersonCertificatesProvider,
    @AppScope appScope: CoroutineScope,
    private val dccWalletInfoCalculationManager: DccWalletInfoCalculationManager,
    private val dccWalletInfoCleaner: DccWalletInfoCleaner
) {

    init {
        appScope.launch {
            personCertificateProvider.personCertificates
                .drop(1) // Drop first value on App start that causes unnecessary calculation

                // Compare persons emissions certificates by using its hash. Changes in certificates set such as
                // registering, recycling, restoring, retrieving, re-issuing a DCC will lead to a difference in the set
                // of qrCode hashes and therefore  will calculate the DccWalletInfo. Any change in the flow that is not
                // meant to trigger calculation - such as badge dismissal or validity state change - is not considered
                .distinctUntilChangedBy { it.sortedQrCodeHashSet }
                .collectLatest {
                    runCatching { triggerNow(admissionScenarioId()) }.onFailure {
                        Timber.tag(TAG).d(it, "Failed to calculate dccWallet")
                    }
                }
        }
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
                    admissionScenarioId = admissionScenarioId()
                ),
                originTag = TAG
            )
        )
    }

    suspend fun triggerNow(admissionScenarioId: String) {
        dccWalletInfoCalculationManager.triggerCalculationAfterCertificateChange(admissionScenarioId)
        dccWalletInfoCleaner.clean()
    }

    private val Set<PersonCertificates>.sortedQrCodeHashSet: Set<String>
        get() = flatMap { personCert -> personCert.certificates.map { it.qrCodeHash } }.sorted().toSet()

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
