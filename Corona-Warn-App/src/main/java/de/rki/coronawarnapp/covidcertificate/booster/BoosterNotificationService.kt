package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.notification.DccWalletInfoNotificationService
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.notification.PersonNotificationSender
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoosterNotificationService @Inject constructor(
    private val personNotificationSender: PersonNotificationSender,
    private val vaccinationRepository: VaccinationRepository,
    private val timeStamper: TimeStamper,
) : DccWalletInfoNotificationService {
    override suspend fun notifyIfNecessary(
        personIdentifier: CertificatePersonIdentifier,
        oldWalletInfo: DccWalletInfo?,
        newWalletInfo: DccWalletInfo
    ) {

        Timber.tag(TAG).v("notifyIfNecessary() - Started")

        val newRuleId = newWalletInfo.boosterNotification.identifier ?: run {
            Timber.d("Showing no notification since the ruleId of the walletInfo is null.")
            vaccinationRepository.clearBoosterRuleInfo(personIdentifier)
            return
        }
        val oldRuleId = oldWalletInfo?.boosterNotification?.identifier

        val codeSHA256 = personIdentifier.codeSHA256
        Timber.tag(TAG)
            .d(
                "BoosterRule of person=%s  ruleIdOldWalletInfo=%s, ruleIdNewWalletInfo=%s",
                codeSHA256,
                oldRuleId,
                newRuleId
            )

        if (newRuleId != oldRuleId) {
            Timber.tag(TAG).d("Notifying person=%s about rule=%s", codeSHA256, newRuleId)
            personNotificationSender.showNotification(personIdentifier)
            // Clears booster rule last seen badge, to be shown in conjunction with notification
            vaccinationRepository.clearBoosterRuleInfo(personIdentifier)
            vaccinationRepository.updateBoosterNotifiedAt(personIdentifier, timeStamper.nowUTC)
            Timber.tag(TAG).d("Person %s notified about booster rule change", codeSHA256)
        } else {
            Timber.tag(TAG).d("Person %s shouldn't be notified about booster rule=%s", codeSHA256, newRuleId)
        }

        Timber.tag(TAG).v("notifyIfNecessary() - Finished")
    }

    companion object {
        private val TAG = tag<BoosterNotificationService>()
    }
}
