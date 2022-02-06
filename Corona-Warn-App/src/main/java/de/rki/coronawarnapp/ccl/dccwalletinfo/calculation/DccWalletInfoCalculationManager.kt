package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.booster.BoosterNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    /**
     * Trigger [DccWalletInfo] calculation for all persons
     */
    suspend fun triggerCalculation(configurationChanged: Boolean = true) {
        initCalculation()
        val persons = personCertificatesProvider.personCertificates.first()
        Timber.d("triggerCalculation() for [%d] persons", persons.size)

        val personGroupKeys = persons.mapNotNull { it.personIdentifier?.groupingKey }
        val dccWalletGroupKeys = dccWalletInfoRepository.personWallets.first().map { it.personGroupKey }
        val idsToClean = dccWalletGroupKeys subtract personGroupKeys
        Timber.d("Cleaning DccWalletInfo for [%d] persons", idsToClean.size)
        // Cleanup DccWalletInfo for persons who don't have certificates any longer
        // i.e all their certificates are recycled or have been deleted permanently.
        // Note: This is `NOT` affecting newly added persons who don't have DccWalletInfo yet
        dccWalletInfoRepository.delete(idsToClean.toSet())

        val now = timeStamper.nowUTC
        persons.forEach { person ->
            if (configurationChanged ||
                person.dccWalletInfo == null ||
                person.dccWalletInfo.validUntilInstant.isBefore(now)
            ) {
                updateWalletInfoForPerson(person)
            }
        }
    }

    /**
     * Trigger [DccWalletInfo] calculation for specific person
     */
    suspend fun triggerCalculationForPerson(personIdentifier: CertificatePersonIdentifier) {
        personCertificatesProvider.personCertificates.first()
            .find { it.personIdentifier == personIdentifier }
            ?.let {
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
            val walletInfo = calculation.getDccWalletInfo(person.certificates)
            dccWalletInfoRepository.save(
                person.personIdentifier ?: return,
                walletInfo
            )
            // TODO add when merged
//            boosterNotificationService.notifyIfNecessary(
//                personIdentifier = person.personIdentifier,
//                oldWalletInfo = person.dccWalletInfo,
//                newWalletInfo = walletInfo,
//            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate DccWalletInfo for ${person.personIdentifier}")
        }
    }
}
