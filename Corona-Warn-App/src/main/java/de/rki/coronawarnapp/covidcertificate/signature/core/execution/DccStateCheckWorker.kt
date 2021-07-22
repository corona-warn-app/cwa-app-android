package de.rki.coronawarnapp.covidcertificate.signature.core.execution

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.signature.ui.notification.DscCheckNotification
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import kotlinx.coroutines.flow.first

class DccStateCheckWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dccStateChecker: DccStateChecker,
    private val dscCheckNotification: DscCheckNotification,
    private val vaccinationRepository: VaccinationRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        vaccinationRepository.vaccinationInfos.first().forEach {
            val certificate = it.getMostRecentVaccinationCertificate
            when (certificate.getState()) {
                is CwaCovidCertificate.State.ExpiringSoon -> showNotificationExpiringSoon(certificate.containerId)
                is CwaCovidCertificate.State.Expired -> showNotificationExpired(certificate.containerId)
                is CwaCovidCertificate.State.Invalid -> TODO()
                is CwaCovidCertificate.State.Valid -> {/*all good*/
                }
            }
        }

        recoveryCertificateRepository.certificates.first().forEach {
            val certificate = it.recoveryCertificate
            when (certificate.getState()) {
                is CwaCovidCertificate.State.ExpiringSoon -> showNotificationExpiringSoon(certificate.containerId)
                is CwaCovidCertificate.State.Expired -> showNotificationExpired(certificate.containerId)
                is CwaCovidCertificate.State.Invalid -> TODO()
                is CwaCovidCertificate.State.Valid -> {/*all good*/
                }
            }
        }

        return Result.retry()
    }

    private fun showNotificationExpired(containerId: CertificateContainerId) {
    }

    private fun showNotificationExpiringSoon(containerId: CertificateContainerId) {
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<DccStateCheckWorker>

    companion object {
        private val TAG = DccStateCheckWorker::class.java.simpleName
    }
}
