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
    private val dccBoosterRulesRepository: BoosterRulesRepository,
    private val timeStamper: TimeStamper,
) {
    private val mutex = Mutex()

    suspend fun checkBoosterNotification() = mutex.withLock {
        Timber.tag(TAG).v("checkBoosterNotification()")

        val lastCheck = covidCertificateSettings.lastDccBoosterCheck.value

        if (lastCheck.toLocalDateUtc() == timeStamper.nowUTC.toLocalDateUtc()) {
            Timber.tag(TAG).d("Last check was within 24h, skipping.")
            return
        }

        val allPersons = personCertificatesProvider.personCertificates.first()
        Timber.tag(TAG).d("All persons=%s", allPersons)

        val vaccinatedPersonsMap = vaccinationRepository.vaccinationInfos.first().associateBy { it.identifier }
        Timber.tag(TAG).d("Vaccinated persons=%s", vaccinatedPersonsMap.keys)

        val boosterRules = dccBoosterRulesRepository.rules.first()
        Timber.tag(TAG).d("Booster rules=%s", boosterRules)

        allPersons.forEach { person ->
            try {
                val vaccinatedPerson = vaccinatedPersonsMap[person.personIdentifier]
                if (vaccinatedPerson == null) {
                    Timber.tag(TAG).d("Person %s isn't vaccinated yet", person.personIdentifier)
                    return@forEach
                }
                val rule = dccBoosterRulesValidator.validateBoosterRules(person.certificates, boosterRules)
                Timber.tag(TAG).d("Booster rule= %s for person=%s ", rule, person.personIdentifier)

                notifyIfBoosterChanged(vaccinatedPerson, rule)
                vaccinationRepository.updateBoosterRule(vaccinatedPerson.identifier, rule)
            } catch (e: Exception) {
                Timber.tag(TAG).d(e, "Booster rules check for %s failed", person.personIdentifier)
            }
        }

        covidCertificateSettings.lastDccBoosterCheck.update { timeStamper.nowUTC }
    }

    private suspend fun notifyIfBoosterChanged(
        vaccinatedPerson: VaccinatedPerson,
        rule: DccValidationRule?
    ) {
        val personIdentifier = vaccinatedPerson.identifier
        if (rule?.identifier.isNullOrEmpty() &&
            rule?.identifier != vaccinatedPerson.data.lastSeenBoosterRuleIdentifier) {
            Timber.tag(TAG).d(
                "Booster rule of person %s changed from %s to %s",
                personIdentifier,
                vaccinatedPerson.data.lastSeenBoosterRuleIdentifier,
                rule?.identifier
            )

            boosterNotification.showBoosterNotification(personIdentifier)
            Timber.tag(TAG).d("Person %s notified about booster rule change", personIdentifier)

            vaccinationRepository.updateBoosterNotifiedAt(personIdentifier, timeStamper.nowUTC)
        }
    }

    companion object {
        private val TAG = BoosterNotificationService::class.simpleName
    }
}
