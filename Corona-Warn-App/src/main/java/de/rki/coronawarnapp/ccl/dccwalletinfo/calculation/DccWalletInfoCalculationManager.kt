package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.notification.DccWalletInfoNotificationService
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DccWalletInfoCalculationManager @Inject constructor(
    private val boosterRulesRepository: BoosterRulesRepository,
    private val notificationServices: Set<@JvmSuppressWildcards DccWalletInfoNotificationService>,
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val dccWalletInfoRepository: DccWalletInfoRepository,
    private val calculation: DccWalletInfoCalculation,
    private val timeStamper: TimeStamper,
    private val dccValidationRepository: DccValidationRepository
) {

    /**
     * Trigger [DccWalletInfo] calculation for all persons
     */
    suspend fun triggerAfterConfigChange(
        admissionScenarioId: String,
        configurationChanged: Boolean = true
    ): Result = try {
        initCalculation()
        val persons = personCertificatesProvider.personCertificates.first()
        Timber.d("triggerAfterConfigChange() - STARTED")
        val now = timeStamper.nowUTC
        persons.forEach { person ->
            if (configurationChanged ||
                person.dccWalletInfo == null ||
                person.dccWalletInfo.validUntilInstant.isBefore(now)
            ) {
                updateWalletInfoForPerson(person, admissionScenarioId)
            }
        }

        Timber.d("triggerAfterConfigChange() - ENDED")
        Result.Success
    } catch (e: Exception) {
        Timber.d(e, "Failed to run calculation.")
        Result.Failure(e)
    }

    suspend fun triggerNow(
        admissionScenarioId: String
    ): Result = try {
        Timber.d("triggerNow() - STARTED")
        initCalculation()
        val persons = personCertificatesProvider.personCertificates.first()
        Timber.d("triggerNow() for [%d] persons", persons.size)
        persons.forEach {
            updateWalletInfoForPerson(it, admissionScenarioId)
        }

        Timber.d("triggerNow() - ENDED")
        Result.Success
    } catch (e: Exception) {
        Timber.d(e, "Failed to run calculation.")
        Result.Failure(e)
    }

    private suspend fun initCalculation() {
        calculation.init(
            boosterRules = boosterRulesRepository.rules.first(),
            invalidationRules = dccValidationRepository.invalidationRules.first()
        )
    }

    private suspend fun updateWalletInfoForPerson(
        person: PersonCertificates,
        admissionScenarioId: String
    ) = try {
        val personIdentifier = person.personIdentifier
        Timber.d("updateWalletInfoForPerson(person=${personIdentifier.codeSHA256})")

        val newWalletInfo = calculation.getDccWalletInfo(
            person.certificates,
            admissionScenarioId
        )

        notificationServices.forEach { service ->
            service.notifyIfNecessary(
                personIdentifier = personIdentifier,
                oldWalletInfo = person.dccWalletInfo,
                newWalletInfo = newWalletInfo
            )
        }

        dccWalletInfoRepository.save(
            personIdentifier,
            newWalletInfo
        )

        Result.Success
    } catch (e: Exception) {
        Timber.e(e, "Failed to run calculation for person=%s", person.personIdentifier)
        Result.Failure(e)
    }

    sealed class Result {
        object Success : Result()
        data class Failure(val error: Exception) : Result()
    }
}
