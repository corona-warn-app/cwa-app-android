package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DccExpirationCheck @Inject constructor(
    private val dscCheckNotification: DccExpirationNotification,
    private val dscCheckNotificationRepository: DccExpirationNotificationRepository,
    private val vaccinationRepository: VaccinationRepository,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
) {

    suspend fun checkExpirationStates() {
        if (dscCheckNotificationRepository.latestExecutionHasBeenToday()) {
            Timber.i("Latest execution has been today. Abort.")
            return
        }

        val expiredList: MutableList<CertificateContainerId> = mutableListOf()
        val expiringSoonList: MutableList<CertificateContainerId> = mutableListOf()
        vaccinationRepository.vaccinationInfos.first().forEach {
            val certificate = it.getMostRecentVaccinationCertificate
            when (certificate.getState()) {
                is CwaCovidCertificate.State.ExpiringSoon -> expiringSoonList.add(certificate.containerId)
                is CwaCovidCertificate.State.Expired -> expiredList.add(certificate.containerId)
                else -> { /*not relevant*/
                }
            }
        }
        recoveryCertificateRepository.certificates.first().forEach {
            val certificate = it.recoveryCertificate
            when (certificate.getState()) {
                is CwaCovidCertificate.State.ExpiringSoon -> expiringSoonList.add(certificate.containerId)
                is CwaCovidCertificate.State.Expired -> expiredList.add(certificate.containerId)
                else -> { /*not relevant*/
                }
            }
        }

        if (expiredList.isEmpty() && expiringSoonList.isEmpty()) return

        expiredList.forEach {
            if (!it.hasHadExpiredNotification()) {
                showNotificationExpired(it)
                return@forEach
            }
        }

        expiringSoonList.forEach {
            if (!it.hasHadExpiringSoonNotification()) {
                showNotificationExpiringSoon(it)
                return@forEach
            }
        }
    }

    private suspend fun CertificateContainerId.hasHadExpiredNotification(): Boolean {
        return dscCheckNotificationRepository.hasExpiredNotificationBeenShownFor(this)
    }

    private suspend fun CertificateContainerId.hasHadExpiringSoonNotification(): Boolean {
        return dscCheckNotificationRepository.hasExpiringSoonNotificationBeenShownFor(this)
    }

    private suspend fun showNotificationExpired(containerId: CertificateContainerId) {
        dscCheckNotification.showCheckNotification(containerId)
        dscCheckNotificationRepository.reportExpiredNotificationShownFor(containerId)
    }

    private suspend fun showNotificationExpiringSoon(containerId: CertificateContainerId) {
        dscCheckNotification.showCheckNotification(containerId)
        dscCheckNotificationRepository.reportExpiringSoonNotificationShownFor(containerId)
    }
}
