package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccExpirationNotificationService @Inject constructor(
    private val dscCheckNotification: DccExpirationNotification,
    private val vaccinationRepository: VaccinationRepository,
    private val recoveryRepository: RecoveryCertificateRepository,
    private val covidCertificateSettings: CovidCertificateSettings,
    private val timeStamper: TimeStamper,
) {
    private val mutex = Mutex()

    suspend fun showNotificationIfExpired() = mutex.withLock {
        Timber.tag(TAG).v("checkStates()")

        val lastCheck = covidCertificateSettings.lastDccStateBackgroundCheck.value

        if (lastCheck.toLocalDateUtc() == timeStamper.nowUTC.toLocalDateUtc()) {
            Timber.tag(TAG).d("Last check was within 24h, skipping.")
            return
        }

        val allCerts = getCertificates()

        allCerts
            .filter { it.getState() is CwaCovidCertificate.State.Expired }
            .firstOrNull {
                Timber.tag(TAG).w("Certificate expired: %s", it)
                it.notifiedExpiredAt == null
            }
            ?.let {
                if (dscCheckNotification.showNotification(it.containerId)) {
                    setStateNotificationShown(it)
                }
            }

        allCerts
            .filter { it.getState() is CwaCovidCertificate.State.ExpiringSoon }
            .firstOrNull {
                Timber.tag(TAG).w("Certificate expiring soon: %s", it)
                it.notifiedExpiresSoonAt == null && it.notifiedExpiredAt == null
            }
            ?.let {
                if (dscCheckNotification.showNotification(it.containerId)) {
                    setStateNotificationShown(it)
                }
            }

        allCerts
            .filter { it.getState() is CwaCovidCertificate.State.Invalid }
            .firstOrNull {
                Timber.tag(TAG).w("Certificate is invalid: %s", it)
                // TODO clarify whether notification should be shown for every state change or once
                it.notifiedInvalidAt == null && it.notifiedExpiredAt == null &&
                    it.notifiedExpiresSoonAt == null
            }
            ?.let {
                if (dscCheckNotification.showNotification(it.containerId)) {
                    setStateNotificationShown(it)
                }
            }

        covidCertificateSettings.lastDccStateBackgroundCheck.update { timeStamper.nowUTC }
    }

    private suspend fun setStateNotificationShown(certificate: CwaCovidCertificate) {
        val now = timeStamper.nowUTC
        val state = certificate.getState()
        when (certificate) {
            is RecoveryCertificate -> recoveryRepository.setNotifiedState(certificate.containerId, state, now)
            is VaccinationCertificate -> vaccinationRepository.setNotifiedState(certificate.containerId, state, now)
            else -> throw UnsupportedOperationException("Class: ${certificate.javaClass.simpleName}")
        }
    }

    private suspend fun getCertificates(): Set<CwaCovidCertificate> {
        val vacCerts = vaccinationRepository.vaccinationInfos.first().map { it.vaccinationCertificates }.flatten()
        Timber.tag(TAG).d("Checking %d vaccination certificates", vacCerts.size)
        val recCerts = recoveryRepository.certificates.first().map { it.recoveryCertificate }
        Timber.tag(TAG).d("Checking %d recovery certificates", recCerts.size)

        return (vacCerts + recCerts).toSet()
    }

    companion object {
        private val TAG = DccExpirationNotificationService::class.java.simpleName
    }
}
