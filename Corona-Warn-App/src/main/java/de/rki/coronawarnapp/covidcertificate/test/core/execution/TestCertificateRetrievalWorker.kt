package de.rki.coronawarnapp.covidcertificate.test.core.execution

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber

class TestCertificateRetrievalWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val testCertificateRepository: TestCertificateRepository,
    private val dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.tag(TAG).d("$id doWork() failed after $runAttemptCount attempts. Aborting...")
            return Result.failure()
        }

        return try {
            Timber.tag(TAG).v("Refreshing test certificates.")
            val results = testCertificateRepository.refresh()

            if (results.any { it.error != null }) {
                Timber.tag(TAG).w("Some test certificates failed refresh, will retry.")
                Result.retry()
            } else {
                Timber.tag(TAG).d("No errors during test certificate refresh :).")
                dccWalletInfoUpdateTrigger.triggerDccWalletInfoUpdateNow()
                Result.success()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Test result retrieval worker failed.")
            Result.retry()
        }
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<TestCertificateRetrievalWorker>

    companion object {
        private val TAG = TestCertificateRetrievalWorker::class.java.simpleName
    }
}
