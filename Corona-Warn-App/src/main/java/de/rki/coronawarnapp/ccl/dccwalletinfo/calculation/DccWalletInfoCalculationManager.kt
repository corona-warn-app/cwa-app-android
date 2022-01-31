package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DccWalletInfoCalculationManager @Inject constructor(
    private val cclConfigurationRepository: CCLConfigurationRepository,
    private val boosterRulesRepository: BoosterRulesRepository,
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val dccWalletInfoRepository: DccWalletInfoRepository,
    private val calculation: DccWalletInfoCalculation,
    private val timeStamper: TimeStamper,
) {

    suspend fun triggerCalculation(
        configurationChanged: Boolean = true
    ) {
        Timber.d("triggerCalculation()")
        val now = timeStamper.nowUTC
        initCalculation()
        personCertificatesProvider.personCertificates.first().forEach {
            if (configurationChanged ||
                it.dccWalletInfo == null ||
                it.dccWalletInfo.validUntilInstant.isBefore(now)
            ) {
                updateWalletInfoForPerson(it)
            }
        }
    }

    suspend fun triggerCalculationForPerson(personIdentifier: CertificatePersonIdentifier) {
        personCertificatesProvider.personCertificates.first().find {
            it.personIdentifier == personIdentifier
        }?.let {
            initCalculation()
            updateWalletInfoForPerson(it)
        }
    }

    private suspend fun initCalculation() {
        val configs = cclConfigurationRepository.cclConfigurations.first()
        calculation.init(
            configs.first(),
            boosterRulesRepository.rules.first()
        )
    }

    private suspend fun updateWalletInfoForPerson(person: PersonCertificates) {
        try {
            val walletInfo = calculation.getDccWalletInfo(person.certificates)
            dccWalletInfoRepository.save(
                person.personIdentifier ?: return,
                walletInfo
            )
        } catch (e: Exception) {
            Timber.e("Failed to calculate DccWalletInfo for ${person.personIdentifier}", e)
        }
    }
}
