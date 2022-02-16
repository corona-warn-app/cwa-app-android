package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.booster.BoosterNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
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
        Timber.e(e, "Failed to run calculation.")
        Result.Failure(e)
    }

    suspend fun triggerCalculationNow(
        admissionScenarioId: String
    ): Result = try {
        initCalculation()
        personCertificatesProvider.personCertificates.first().forEach {
            updateWalletInfoForPerson(it, admissionScenarioId)
        }
        Result.Success
    } catch (e: Exception) {
        Timber.e(e, "Failed to run calculation.")
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
    ) = try {
        val personIdentifier = checkNotNull(person.personIdentifier) {
            "Person identifier is null. Cannot proceed."
        }

        val newWalletInfo = calculation.getDccWalletInfo(
            person.certificates,
            admissionScenarioId
        )

        boosterNotificationService.notifyIfNecessary(
            personIdentifier = personIdentifier,
            oldWalletInfo = person.dccWalletInfo,
            newWalletInfo = newWalletInfo
        )

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
