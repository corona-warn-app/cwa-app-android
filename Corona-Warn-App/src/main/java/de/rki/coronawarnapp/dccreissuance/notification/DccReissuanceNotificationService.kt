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

    override suspend fun notifyIfNecessary(
        personIdentifier: CertificatePersonIdentifier,
        oldWalletInfo: DccWalletInfo?,
        newWalletInfo: DccWalletInfo
    ) {
        val oldCertReissuance = oldWalletInfo?.certificateReissuance
        val newCertReissuance = newWalletInfo.certificateReissuance
        if (newCertReissuance != null && oldCertReissuance == null) {
            Timber.tag(TAG).d("Notify person=%s about Dcc reissuance", personIdentifier.codeSHA256)
            personNotificationSender.showNotification(
                personIdentifier = personIdentifier,
                messageRes = R.string.notification_body_certificate
            )
            personCertificatesSettings.setDccReissuanceNotifiedAt(personIdentifier)
        } else {
            Timber.tag(TAG).d("Person=%s shouldn't be notified about Dcc reissuance", personIdentifier.codeSHA256)
        }
    }

    companion object {
        private val TAG = tag<DccReissuanceNotificationService>()
    }
}
