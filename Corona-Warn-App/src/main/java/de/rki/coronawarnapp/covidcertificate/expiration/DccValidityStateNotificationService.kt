package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Expired
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.ExpiringSoon
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Revoked
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccValidityStateNotificationService @Inject constructor(
    private val dccValidityStateNotification: DccValidityStateNotification,
    private val vaccinationCertificateRepository: VaccinationCertificateRepository,
    private val recoveryRepository: RecoveryCertificateRepository,
    private val covidCertificateSettings: CovidCertificateSettings,
    private val testCertificateRepository: TestCertificateRepository,
    private val timeStamper: TimeStamper,
) {
    private val mutex = Mutex()

    suspend fun showNotificationIfStateChanged(ignoreLastCheck: Boolean = false) = mutex.withLock {
        Timber.tag(TAG).v("showNotificationIfStateChanged(ignoreLastCheck=%s)", ignoreLastCheck)

        val lastCheck = covidCertificateSettings.lastDccStateBackgroundCheck.value

        if (!ignoreLastCheck && lastCheck.toLocalDateUtc() == timeStamper.nowUTC.toLocalDateUtc()) {
            Timber.tag(TAG).d("Last check was within 24h, skipping.")
            return
        }

        val vacRecCerts = getCertificates()

        vacRecCerts
            .filter { it.state is Expired }
            .firstOrNull {
                Timber.tag(TAG).w("Certificate expired: %s", it)
                it.notifiedExpiredAt == null
            }
            ?.let {
                if (dccValidityStateNotification.showNotification(it.containerId)) {
                    setStateNotificationShown(it)
                }
            }

        vacRecCerts
            .filter { it.state is ExpiringSoon }
            .firstOrNull {
                Timber.tag(TAG).w("Certificate expiring soon: %s", it)
                it.notifiedExpiresSoonAt == null && it.notifiedExpiredAt == null
            }
            ?.let {
                if (dccValidityStateNotification.showNotification(it.containerId)) {
                    setStateNotificationShown(it)
                }
            }

        val testCerts = testCertificateRepository.certificates.first().mapNotNull { it.testCertificate }
        Timber.tag(TAG).d("Checking %d test certificates", testCerts.size)
        val allCerts = vacRecCerts + testCerts

        allCerts
            .filter { it.state is Invalid }
            .firstOrNull {
                Timber.tag(TAG).w("Certificate is invalid: %s", it)
                it.notifiedInvalidAt == null
            }
            ?.let {
                if (dccValidityStateNotification.showNotification(it.containerId)) {
                    setStateNotificationShown(it)
                }
            }

        allCerts
            .filter { it.state is Blocked }
            .firstOrNull {
                Timber.tag(TAG).w("Certificate is blocked: %s", it)
                it.notifiedBlockedAt == null
            }
            ?.let {
                if (dccValidityStateNotification.showNotification(it.containerId)) {
                    setStateNotificationShown(it)
                }
            }

        allCerts
            .filter { it.state is Revoked }
            .firstOrNull {
                Timber.tag(TAG).w("Certificate is revoked: %s", it)
                it.notifiedRevokedAt == null
            }
            ?.let {
                if (dccValidityStateNotification.showNotification(it.containerId)) {
                    setStateNotificationShown(it)
                }
            }

        covidCertificateSettings.lastDccStateBackgroundCheck.update { timeStamper.nowUTC }
    }

    private suspend fun setStateNotificationShown(certificate: CwaCovidCertificate) {
        val now = timeStamper.nowUTC
        val state = certificate.state
        when (certificate) {
            is RecoveryCertificate ->
                recoveryRepository.setNotifiedState(certificate.containerId, state, now)
            is VaccinationCertificate ->
                vaccinationCertificateRepository.setNotifiedState(certificate.containerId, state, now)
            is TestCertificate ->
                testCertificateRepository.setNotifiedState(certificate.containerId, state, now)
            else -> throw UnsupportedOperationException("Class: ${certificate.javaClass.simpleName}")
        }
    }

    private suspend fun getCertificates(): Set<CwaCovidCertificate> {
        val vacCerts = vaccinationCertificateRepository.freshCertificates.first().map { it.vaccinationCertificate }
        Timber.tag(TAG).d("Checking %d vaccination certificates", vacCerts.size)
        val recCerts = recoveryRepository.freshCertificates.first().map { it.recoveryCertificate }
        Timber.tag(TAG).d("Checking %d recovery certificates", recCerts.size)

        return (vacCerts + recCerts).toSet()
    }

    companion object {
        private val TAG = tag<DccValidityStateNotificationService>()
    }
}
