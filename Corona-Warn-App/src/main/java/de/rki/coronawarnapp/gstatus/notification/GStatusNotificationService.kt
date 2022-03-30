package de.rki.coronawarnapp.gstatus.notification

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
class GStatusNotificationService @Inject constructor(
    private val personNotificationSender: PersonNotificationSender,
    private val personCertificatesSettings: PersonCertificatesSettings
) : DccWalletInfoNotificationService {

    override suspend fun notifyIfNecessary(
        personIdentifier: CertificatePersonIdentifier,
        oldWalletInfo: DccWalletInfo?,
        newWalletInfo: DccWalletInfo
    ) {
        Timber.tag(TAG).v("notifyIfNecessary() - Started")
        val oldGStatusId = oldWalletInfo?.admissionState?.identifier
        val newGStatusId = newWalletInfo.admissionState.identifier
        when {
            newGStatusId != null && oldGStatusId != null && newGStatusId != oldGStatusId -> {
                Timber.tag(TAG).d("Notifying person =%s about G status change", personIdentifier.codeSHA256)
                personNotificationSender.showNotification(personIdentifier)
                personCertificatesSettings.setGStatusNotifiedAt(personIdentifier)
            }
            newGStatusId == null -> {
                Timber.tag(TAG).d("Don't notify person %s about G status change", personIdentifier.codeSHA256)
                Timber.tag(TAG).d("Dismissing g-status badge for person %s", personIdentifier.codeSHA256)
                personCertificatesSettings.dismissGStatusBadge(personIdentifier)
            }
            else -> {
                Timber.tag(TAG).d("Don't notify person %s about G status change", personIdentifier.codeSHA256)
                personCertificatesSettings.dismissGStatusBadge(personIdentifier)
            }
        }
    }

    companion object {
        private val TAG = tag<GStatusNotificationService>()
    }
}
