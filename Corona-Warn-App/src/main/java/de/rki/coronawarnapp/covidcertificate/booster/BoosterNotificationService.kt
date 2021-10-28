package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoosterNotificationService @Inject constructor(
    private val boosterNotification: BoosterNotification,
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val covidCertificateSettings: CovidCertificateSettings,
    private val dccBoosterRulesValidator: DccBoosterRulesValidator,
    private val vaccinationRepository: VaccinationRepository,
    private val timeStamper: TimeStamper,
) {
    private val mutex = Mutex()

    suspend fun checkBoosterNotification(forceCheck: Boolean = false) = mutex.withLock {
        Timber.tag(TAG).v("checkBoosterNotification() - Started")

        val lastCheck = covidCertificateSettings.lastDccBoosterCheck.value

        if (!forceCheck && lastCheck.toLocalDateUtc() == timeStamper.nowUTC.toLocalDateUtc()) {
            Timber.tag(TAG).d("Last check was within 24h, skipping.")
            return
        }

        val allPersons = personCertificatesProvider.personCertificates.first()
        Timber.tag(TAG).d("All persons=%s", allPersons.map { it.personIdentifier?.codeSHA256 })

        val vaccinatedPersonsMap = vaccinationRepository.vaccinationInfos.first().associateBy { it.identifier }
        Timber.tag(TAG).d("Vaccinated persons=%s", vaccinatedPersonsMap.keys.map { it.codeSHA256 })

        allPersons.forEach { person ->
            val codeSHA256 = person.personIdentifier?.codeSHA256
            try {
                val vaccinatedPerson = vaccinatedPersonsMap[person.personIdentifier]
                if (vaccinatedPerson == null) {
                    Timber.tag(TAG).d("Person %s isn't vaccinated yet", codeSHA256)
                    return@forEach
                }
                Timber.tag(TAG).d("Person %s has %s certificates", codeSHA256, person.certificates.size)
                val rule = dccBoosterRulesValidator.validateBoosterRules(person.certificates)

                // Hold last saved rule before updating to new one
                val lastSavedRuleId = vaccinatedPerson.data.boosterRule?.identifier

                Timber.tag(TAG).d("Saving rule=%s for person=%s", rule, codeSHA256)
                vaccinationRepository.updateBoosterRule(vaccinatedPerson.identifier, rule)

                Timber.tag(TAG).d("Booster rule= %s for person=%s", rule?.identifier, codeSHA256)
                notifyIfBoosterChanged(vaccinatedPerson.identifier, rule, lastSavedRuleId)
            } catch (e: Exception) {
                Timber.tag(TAG).d(e, "Booster rules check for %s failed", codeSHA256)
            }
        }

        covidCertificateSettings.lastDccBoosterCheck.update { timeStamper.nowUTC }
        Timber.tag(TAG).v("checkBoosterNotification() - Finished")
    }

    private suspend fun notifyIfBoosterChanged(
        personIdentifier: CertificatePersonIdentifier,
        rule: DccValidationRule?,
        lastSavedRuleId: String?,
    ) {
        val codeSHA256 = personIdentifier.codeSHA256
        Timber.tag(TAG)
            .d("BoosterRule of person=%s  lastChecked=%s, lastSaved=%s", codeSHA256, rule?.identifier, lastSavedRuleId)
        if (rule?.identifier.isNullOrEmpty().not() && rule?.identifier != lastSavedRuleId) {
            Timber.tag(TAG).d("Notifying person=%s about rule=%s", codeSHA256, rule?.identifier)
            boosterNotification.showBoosterNotification(personIdentifier)
            vaccinationRepository.updateBoosterNotifiedAt(personIdentifier, timeStamper.nowUTC)
            Timber.tag(TAG).d("Person %s notified about booster rule change", codeSHA256)
        } else {
            boosterNotification.cancelNotification(personIdentifier)
            Timber.tag(TAG).d("Person %s shouldn't be notified about booster rule=%s", codeSHA256, rule?.identifier)
        }
    }

    companion object {
        private val TAG = tag<BoosterNotificationService>()
    }
}
