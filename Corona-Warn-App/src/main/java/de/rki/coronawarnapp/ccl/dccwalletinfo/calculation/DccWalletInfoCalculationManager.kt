package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.notification.DccWalletInfoNotificationService
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
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
) {

    /**
     * Trigger [DccWalletInfo] calculation for all persons
     */
    suspend fun triggerCalculationAfterConfigChange(
        admissionScenarioId: String,
        configurationChanged: Boolean = true
    ): Result = try {
        initCalculation()
        val persons = personCertificatesProvider.personCertificates.first()
        Timber.d("triggerCalculation() for [%d] persons", persons.size)
        val now = timeStamper.nowUTC
        persons.forEach { person ->
            if (configurationChanged ||
                person.dccWalletInfo == null ||
                person.dccWalletInfo.validUntilInstant.isBefore(now)
            ) {
                updateWalletInfoForPerson(person, admissionScenarioId)
            }
        }
        Result.Success
    } catch (e: Exception) {
        Timber.d(e, "Failed to run calculation.")
        Result.Failure(e)
    }

    suspend fun triggerCalculationAfterCertificateChange(
        admissionScenarioId: String
    ): Result = try {
        initCalculation()
        personCertificatesProvider.personCertificates.first().forEach {
            updateWalletInfoForPerson(it, admissionScenarioId)
        }
        Result.Success
    } catch (e: Exception) {
        Timber.d(e, "Failed to run calculation.")
        Result.Failure(e)
    }

    private suspend fun initCalculation() {
        calculation.init(
            boosterRulesRepository.rules.first()
        )
    }

    private suspend fun updateWalletInfoForPerson(
        person: PersonCertificates,
        admissionScenarioId: String
    ) {
        val personIdentifier = checkNotNull(person.personIdentifier) {
            "Person identifier is null. Cannot proceed."
        }

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
    }

    sealed class Result {
        object Success : Result()
        data class Failure(val error: Exception) : Result()
    }
}
