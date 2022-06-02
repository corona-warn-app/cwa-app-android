package de.rki.coronawarnapp.dccreissuance.notification

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.notification.DccWalletInfoNotificationService
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.notification.PersonNotificationSender
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccReissuanceNotificationService @Inject constructor(
    private val personNotificationSender: PersonNotificationSender,
    private val personCertificatesSettings: PersonCertificatesSettings,
) : DccWalletInfoNotificationService {

    override val notificationSenderType: Int = 0xDCCAEE

    override suspend fun notifyIfNecessary(
        personIdentifier: CertificatePersonIdentifier,
        oldWalletInfo: DccWalletInfo?,
        newWalletInfo: DccWalletInfo
    ) {
        val oldIdentifier = oldWalletInfo?.certificateReissuance?.reissuanceDivision?.identifier
        when {
            newWalletInfo.hasNewReissuance(oldIdentifier) -> {
                Timber.tag(TAG).d("Notify person=%s about reissuance", personIdentifier.codeSHA256)
                personNotificationSender.showNotification(
                    personIdentifier = personIdentifier,
                    type = notificationSenderType,
                    messageRes = R.string.notification_body_certificate
                )
                personCertificatesSettings.setDccReissuanceNotifiedAt(personIdentifier)
            }
            // no action needed
            newWalletInfo.hasReissuance -> {
                Timber.tag(TAG).d("Person=%s has no changes", personIdentifier.codeSHA256)
            }
            // no reissuance -> dismiss
            else -> {
                Timber.tag(TAG).d("Dismiss badge for person=%s", personIdentifier.codeSHA256)
                personCertificatesSettings.dismissReissuanceBadge(personIdentifier)
            }
        }
    }

    private fun DccWalletInfo.hasNewReissuance(oldIdentifier: String?): Boolean {
        return this.hasReissuance &&
            certificateReissuance?.reissuanceDivision?.identifier != oldIdentifier
    }

    companion object {
        private val TAG = tag<DccReissuanceNotificationService>()
    }
}
