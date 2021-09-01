package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
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

    suspend fun checkBoosterNotification() = mutex.withLock {
        Timber.tag(TAG).v("checkBoosterNotification() - Started")

        val lastCheck = covidCertificateSettings.lastDccBoosterCheck.value

        if (lastCheck.toLocalDateUtc() == timeStamper.nowUTC.toLocalDateUtc()) {
            Timber.tag(TAG).d("Last check was within 24h, skipping.")
            return
        }

        val allPersons = personCertificatesProvider.personCertificates.first()
        Timber.tag(TAG).d("All persons=%s", allPersons.map { it.personIdentifier.codeSHA256 })

        val vaccinatedPersonsMap = vaccinationRepository.vaccinationInfos.first().associateBy { it.identifier }
        Timber.tag(TAG).d("Vaccinated persons=%s", vaccinatedPersonsMap.keys.map { it.codeSHA256 })

        allPersons.forEach { person ->
            val codeSHA256 = person.personIdentifier.codeSHA256
            try {
                val vaccinatedPerson = vaccinatedPersonsMap[person.personIdentifier]
                if (vaccinatedPerson == null) {
                    Timber.tag(TAG).d("Person %s isn't vaccinated yet", codeSHA256)
                    return@forEach
                }
                Timber.tag(TAG).d("Person %s has %s certificates", codeSHA256, person.certificates.size)
                val rule = dccBoosterRulesValidator.validateBoosterRules(person.certificates)

                Timber.tag(TAG).d("Booster rule= %s for person=%s ", rule, codeSHA256)

                vaccinationRepository.updateBoosterRule(vaccinatedPerson.identifier, rule)
                notifyIfBoosterChanged(vaccinatedPerson, rule)
            } catch (e: Exception) {
                Timber.tag(TAG).d(e, "Booster rules check for %s failed", codeSHA256)
            }
        }

        covidCertificateSettings.lastDccBoosterCheck.update { timeStamper.nowUTC }
        Timber.tag(TAG).v("checkBoosterNotification() - Finished")
    }

    private suspend fun notifyIfBoosterChanged(
        vaccinatedPerson: VaccinatedPerson,
        rule: DccValidationRule?
    ) {
        val codeSHA256 = vaccinatedPerson.identifier.codeSHA256
        val lastSeenBoosterRuleIdentifier = vaccinatedPerson.data.lastSeenBoosterRuleIdentifier
        Timber.tag(TAG).d(
            "BoosterRule of person=%s  lastChecked=%s, lastSeen=%s", codeSHA256,
            rule?.identifier, lastSeenBoosterRuleIdentifier
        )
        if (rule?.identifier.isNullOrEmpty().not() && rule?.identifier != lastSeenBoosterRuleIdentifier) {
            Timber.tag(TAG).d("Notifying person=%s about rule=%s", codeSHA256, rule?.identifier)
            boosterNotification.showBoosterNotification(vaccinatedPerson.identifier)
            vaccinationRepository.updateBoosterNotifiedAt(vaccinatedPerson.identifier, timeStamper.nowUTC)
            Timber.tag(TAG).d("Person %s notified about booster rule change", codeSHA256)
        } else {
            Timber.tag(TAG).d("Person %s shouldn't be notified about booster rule=%s", codeSHA256, rule?.identifier)
        }
    }

    companion object {
        private val TAG = BoosterNotificationService::class.simpleName
    }
}
