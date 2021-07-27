package de.rki.coronawarnapp.covidcertificate.signature.core.execution

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.signature.ui.notification.DscCheckNotification
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory

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
        // TODO
        // Check certificate states
        // Show notification if necessary
        return Result.retry()
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<DccStateCheckWorker>

    companion object {
        private val TAG = DccStateCheckWorker::class.java.simpleName
    }
}
