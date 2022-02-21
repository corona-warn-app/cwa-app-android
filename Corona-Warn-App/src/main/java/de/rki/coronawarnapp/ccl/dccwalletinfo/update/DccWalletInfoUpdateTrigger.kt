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
    personCertificateProvider: PersonCertificatesProvider,
    @AppScope appScope: CoroutineScope,
    private val cclSettings: CclSettings,
    private val appConfigProvider: AppConfigProvider,
    private val dccWalletInfoCleaner: DccWalletInfoCleaner,
    private val dccWalletInfoCalculationManager: DccWalletInfoCalculationManager
) {

    init {
        appScope.launch {
            personCertificateProvider.personCertificates
                .drop(1) // Drop first value on App start that causes unnecessary calculation

                /*
                 Compare persons emissions certificates by using its hash. Changes in certificates set such as
                 registering, recycling, restoring, retrieving and, re-issuing a DCC will lead to a difference in the
                 set of qrCode hashes and therefore  will calculate the DccWalletInfo. Any change in the flow that
                 isn't meant to trigger calculation - such as badge dismissal or validity state change -
                 isn't considered
                 */
                .distinctUntilChangedBy { it.sortedQrCodeHashSet }
                .collectLatest {
                    runCatching { triggerNow(admissionScenarioId()) }.onFailure {
                        Timber.tag(TAG).d(it, "Failed to calculate dccWallet")
                    }
                }
        }
    }

    suspend fun triggerAfterConfigChange(configurationChanged: Boolean = false) {
        Timber.tag(TAG).d("triggerAfterConfigChange()")

        dccWalletInfoCalculationManager.triggerAfterConfigChange(
            admissionScenarioId = admissionScenarioId(),
            configurationChanged = configurationChanged
        )
        dccWalletInfoCleaner.clean()
    }

    suspend fun triggerNow(admissionScenarioId: String) {
        Timber.tag(TAG).d("triggerNow()")

        dccWalletInfoCalculationManager.triggerNow(admissionScenarioId = admissionScenarioId)
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
