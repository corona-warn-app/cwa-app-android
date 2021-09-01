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

        allPersons.forEach { person ->
            try {
                val vaccinatedPerson = vaccinatedPersonsMap[person.personIdentifier]
                if (vaccinatedPerson == null) {
                    Timber.tag(TAG).d("Person %s isn't vaccinated yet", person.personIdentifier)
                    return@forEach
                }
                val rule = dccBoosterRulesValidator.validateBoosterRules(person.certificates)
                Timber.tag(TAG).d("Booster rule= %s for person=%s ", rule, person.personIdentifier)

                vaccinationRepository.updateBoosterRule(vaccinatedPerson.identifier, rule)
                notifyIfBoosterChanged(vaccinatedPerson, rule)
            } catch (e: Exception) {
                Timber.tag(TAG).d(e, "Booster rules check for %s failed", person.personIdentifier)
            }
        }

        covidCertificateSettings.lastDccBoosterCheck.update { timeStamper.nowUTC }
        Timber.tag(TAG).v("checkBoosterNotification() finished")
    }

    private suspend fun notifyIfBoosterChanged(
        vaccinatedPerson: VaccinatedPerson,
        rule: DccValidationRule?
    ) {
        val identifier = vaccinatedPerson.identifier
        val lastSeenBoosterRuleIdentifier = vaccinatedPerson.data.lastSeenBoosterRuleIdentifier
        Timber.tag(TAG).d(
            "BoosterRule of person=%s  lastChecked=%s, lastSeen=%s", identifier.codeSHA256,
            rule?.identifier, lastSeenBoosterRuleIdentifier
        )
        if (rule?.identifier.isNullOrEmpty().not() && rule?.identifier != lastSeenBoosterRuleIdentifier) {
            Timber.tag(TAG).d("Notifying person=%s about rule=%s", identifier, rule?.identifier)
            boosterNotification.showBoosterNotification(identifier)
            vaccinationRepository.updateBoosterNotifiedAt(identifier, timeStamper.nowUTC)
            Timber.tag(TAG).d("Person %s notified about booster rule change", identifier.codeSHA256)
        } else {
            Timber.tag(TAG).d("Person %s isn't notified about booster rule=%s", identifier.codeSHA256, rule?.identifier)
        }
    }

    companion object {
        private val TAG = BoosterNotificationService::class.simpleName
    }
}
