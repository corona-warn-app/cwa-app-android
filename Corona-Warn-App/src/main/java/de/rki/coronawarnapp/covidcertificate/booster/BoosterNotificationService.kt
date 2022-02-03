package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoosterNotificationService @Inject constructor(
    private val boosterNotificationSender: BoosterNotificationSender,
    private val vaccinationRepository: VaccinationRepository,
    private val timeStamper: TimeStamper,
) {
    suspend fun notifyIfNecessary(
        personIdentifier: CertificatePersonIdentifier,
        oldWalletInfo: DccWalletInfo,
        newWalletInfo: DccWalletInfo
    ) {

        Timber.tag(TAG).v("notifyIfNecessary() - Started")

        val newRuleId = newWalletInfo.boosterNotification.identifier ?: run {
            Timber.d("Showing no notification since the ruleId of the walletInfo is null.")
            vaccinationRepository.clearBoosterRuleInfo(personIdentifier)
            return
        }
        val oldRuleId = oldWalletInfo.boosterNotification.identifier

        // In versions prior to 2.18, the booster rule identifier was stored in VaccinatedPerson.data. From 2.18 onwards,
        // storing this identifier there is not necessary anymore, since this information is kept in the DccWalletInfo.
        // However, we need to check if the user already saw the booster notification in a version prior to 2.18
        val legacyBoosterRuleId = getLegacyRuleId(personIdentifier)

        val codeSHA256 = personIdentifier.codeSHA256
        Timber.tag(TAG)
            .d(
                "BoosterRule of person=%s  ruleIdOldWalletInfo=%s, ruleIdNewWalletInfo=%s, legacyBoosterRuleId=%s",
                codeSHA256,
                oldRuleId,
                newRuleId,
                legacyBoosterRuleId
            )

        if (newRuleId != oldRuleId && newRuleId != legacyBoosterRuleId) {
            Timber.tag(TAG).d("Notifying person=%s about rule=%s", codeSHA256, newRuleId)
            boosterNotificationSender.showBoosterNotification(personIdentifier)
            vaccinationRepository.updateBoosterNotifiedAt(personIdentifier, timeStamper.nowUTC)
            Timber.tag(TAG).d("Person %s notified about booster rule change", codeSHA256)
        } else {
            Timber.tag(TAG).d("Person %s shouldn't be notified about booster rule=%s", codeSHA256, newRuleId)
        }

        Timber.tag(TAG).v("notifyIfNecessary() - Finished")
    }

    private suspend fun getLegacyRuleId(personIdentifier: CertificatePersonIdentifier): String? {
        val vaccinatedPersonsMap = vaccinationRepository.vaccinationInfos.first().associateBy { it.identifier }
        return vaccinatedPersonsMap[personIdentifier]?.data?.boosterRuleIdentifier
    }

    companion object {
        private val TAG = tag<BoosterNotificationService>()
    }
}
