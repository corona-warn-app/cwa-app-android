package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.booster.BoosterNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DccWalletInfoCalculationManager @Inject constructor(
    private val boosterRulesRepository: BoosterRulesRepository,
    private val boosterNotificationService: BoosterNotificationService,
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val dccWalletInfoRepository: DccWalletInfoRepository,
    private val calculation: DccWalletInfoCalculation,
    private val timeStamper: TimeStamper,
) {

    suspend fun triggerCalculationAfterConfigChange(
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

    suspend fun triggerCalculationAfterCertificateChange() {
        personCertificatesProvider.personCertificates.first().forEach {
            updateWalletInfoForPerson(it)
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
        calculation.init(
            boosterRulesRepository.rules.first()
        )
    }

    private suspend fun updateWalletInfoForPerson(person: PersonCertificates) {
        try {
            val personIdentifier = checkNotNull(person.personIdentifier) {
                "Person identifier is null. Cannot proceed."
            }

            val newWalletInfo = calculation.getDccWalletInfo(person.certificates)

            boosterNotificationService.notifyIfNecessary(
                personIdentifier = personIdentifier,
                oldWalletInfo = person.dccWalletInfo,
                newWalletInfo = newWalletInfo
            )

            dccWalletInfoRepository.save(
                personIdentifier,
                newWalletInfo
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate DccWalletInfo for ${person.personIdentifier}")
        }
    }
}
