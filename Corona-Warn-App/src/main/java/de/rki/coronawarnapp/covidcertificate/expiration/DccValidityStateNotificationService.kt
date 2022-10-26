package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
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
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUtc
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccValidityStateNotificationService @Inject constructor(
    private val stateNotification: DccValidityStateNotification,
    private val vcRepo: VaccinationCertificateRepository,
    private val rcRepo: RecoveryCertificateRepository,
    private val tcRepo: TestCertificateRepository,
    private val covidCertificateSettings: CovidCertificateSettings,
    private val timeStamper: TimeStamper,
) {
    private val mutex = Mutex()

    suspend fun showNotificationIfStateChanged(forceCheck: Boolean = false) = mutex.withLock {
        Timber.tag(TAG).v("showNotificationIfStateChanged(forceCheck=%s)", forceCheck)
        val lastCheck = covidCertificateSettings.lastDccStateBackgroundCheck.value
        val timeBasedCheckRequired = lastCheck.toLocalDateUtc() == timeStamper.nowUTC.toLocalDateUtc()

        if (!forceCheck && timeBasedCheckRequired) {
            Timber.tag(TAG).d("Last check is within the same day -> skipping for now (see you tomorrow).")
            return
        }

        val allCerts = getCertificates()
        allCerts.notifyForState<Invalid> { it.notifiedInvalidAt == null }
        allCerts.notifyForState<Blocked> { it.notifiedBlockedAt == null }
        allCerts.notifyForState<Revoked> { it.notifiedRevokedAt == null }
        covidCertificateSettings.lastDccStateBackgroundCheck.update { timeStamper.nowUTC }
    }

    private suspend fun CwaCovidCertificate.setStateNotificationShown(
        time: Instant = timeStamper.nowUTC
    ) = when (this) {
        is TestCertificate -> tcRepo.setNotifiedState(containerId, state, time)
        is RecoveryCertificate -> rcRepo.setNotifiedState(containerId, state, time)
        is VaccinationCertificate -> vcRepo.setNotifiedState(containerId, state, time)
        else -> throw UnsupportedOperationException("Class: ${javaClass.simpleName}")
    }

    private suspend inline fun <reified S : State> Set<CwaCovidCertificate>.notifyForState(
        predicate: (CwaCovidCertificate) -> Boolean
    ) = filter { it.state is S }
        .firstOrNull(predicate)
        ?.let {
            Timber.tag(TAG).w("Certificate is ${S::class.simpleName}: %s", it)
            if (stateNotification.showNotification(it.containerId)) it.setStateNotificationShown()
        }

    private suspend fun getCertificates(): Set<CwaCovidCertificate> {
        val vacCerts = vcRepo.certificates.first().map { it.vaccinationCertificate }
        Timber.tag(TAG).d("Checking [%d] vaccination certificates", vacCerts.size)
        val recCerts = rcRepo.certificates.first().map { it.recoveryCertificate }
        Timber.tag(TAG).d("Checking [%d] recovery certificates", recCerts.size)
        val testCerts = tcRepo.certificates.first().mapNotNull { it.testCertificate }
        Timber.tag(TAG).d("Checking [%d] test certificates", testCerts.size)
        return vacCerts.plus(recCerts).plus(testCerts).toSet()
    }

    companion object {
        private val TAG = tag<DccValidityStateNotificationService>()
    }
}
