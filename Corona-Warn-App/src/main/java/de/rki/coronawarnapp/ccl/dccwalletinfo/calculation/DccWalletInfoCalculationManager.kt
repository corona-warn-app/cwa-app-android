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
    suspend fun triggerCalculationAfterConfigChange(configurationChanged: Boolean = true) = try {
        initCalculation()
        val persons = personCertificatesProvider.personCertificates.first()
        Timber.d("triggerCalculation() for [%d] persons", persons.size)
        val now = timeStamper.nowUTC
        persons.forEach { person ->
            if (configurationChanged ||
                person.dccWalletInfo == null ||
                person.dccWalletInfo.validUntilInstant.isBefore(now)
            ) {
                updateWalletInfoForPerson(person)
            }
        }
    } catch (e: Exception) {
        Timber.d(e, "Failed to run calculation.")
    }

    suspend fun triggerCalculationAfterCertificateChange() = try {
        initCalculation()
        personCertificatesProvider.personCertificates.first().forEach {
            updateWalletInfoForPerson(it)
        }
    } catch (e: Exception) {
        Timber.d(e, "Failed to run calculation.")
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
            Timber.d(e, "Failed to calculate DccWalletInfo for ${person.personIdentifier}")
        }
    }
}
