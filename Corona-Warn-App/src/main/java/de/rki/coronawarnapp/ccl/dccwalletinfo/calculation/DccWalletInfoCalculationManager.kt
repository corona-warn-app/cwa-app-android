package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.configuration.storage.CClConfigurationRepository
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import javax.inject.Inject

class DccWalletInfoCalculationManager @Inject constructor(
    private val cclConfigurationRepository: CClConfigurationRepository,
    private val boosterRulesRepository: BoosterRulesRepository,
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val calculation: DccWalletInfoCalculation,
) {

    fun triggerCalculation(
        configurationChanged: Boolean = true
    ) {
        // TODO
    }

    fun triggerCalculationForPerson(personIdentifier: CertificatePersonIdentifier) {
        // TODO
    }
}
