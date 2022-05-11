package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccadmission.model.Scenario
import de.rki.coronawarnapp.ccl.dccwalletinfo.DccWalletInfoCleaner
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccWalletInfoUpdateTrigger @Inject constructor(
    @AppScope appScope: CoroutineScope,
    private val cclSettings: CclSettings,
    private val appConfigProvider: AppConfigProvider,
    private val dccWalletInfoCleaner: DccWalletInfoCleaner,
    private val personCertificateProvider: PersonCertificatesProvider,
    private val personCertificatesSettings: PersonCertificatesSettings,
    private val dccWalletInfoCalculationManager: DccWalletInfoCalculationManager,
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
                // delay to collect rapid changes and do only one recalculation
                .debounce(1000L)
                .collectLatest {
                    runCatching {
                        triggerNow(admissionScenarioId())
                    }.onFailure {
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
        cleanupAfterCalculation()
    }

    suspend fun triggerNow(admissionScenarioId: String) {
        Timber.tag(TAG).d("triggerNow()")
        dccWalletInfoCalculationManager.triggerNow(admissionScenarioId = admissionScenarioId)
        cleanupAfterCalculation()
    }

    private suspend fun cleanupAfterCalculation() {
        dccWalletInfoCleaner.clean()

        val personIdentifiers = personCertificateProvider.personCertificates.first()
            .map { it.personIdentifier }.toSet()

        /*
        After person certificates change a merge or a split could happen and this will lead to outdated PersonSettings,
        therefore we need to clean after the calculation which is triggering person notifications / badges
         */
        personCertificatesSettings.cleanSettingsNotIn(personIdentifiers)
    }

    private val Set<PersonCertificates>.sortedQrCodeHashSet: Set<String>
        get() = flatMap { personCert -> personCert.certificates.map { it.qrCodeHash } }.sorted().toSet()

    private suspend fun admissionScenarioId(): String =
        if (appConfigProvider.getAppConfig().admissionScenariosEnabled) {
            cclSettings.admissionScenarioId()
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
