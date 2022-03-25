package de.rki.coronawarnapp.coronatest.type.pcr.execution

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundConstants
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Diagnosis test result retrieval by periodic polling
 */
class PCRResultRetrievalWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val coronaTestRepository: CoronaTestRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.tag(TAG).d("$id doWork() failed after $runAttemptCount attempts. Resuming at normal period.")
            return Result.failure()
        }

        try {
            val pcrTest = coronaTestRepository.latestPCRT.first()
            Timber.tag(TAG).v("Current PCR test: %s", pcrTest)

            if (pcrTest == null) {
                // PCRResultScheduler will cancel us if test is null or test.isFinal == true
                Timber.tag(TAG).d(" $id Stopping worker, there is no PCR test.")
                return Result.success()
            }

            Timber.tag(TAG).v("$id Running PCR test result refresh.")
            coronaTestRepository.refresh(type = BaseCoronaTest.Type.PCR)
            Timber.tag(TAG).d("$id: PCR test result refreshed.")

            return Result.success()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Test result retrieval worker failed.")
            return Result.retry()
        }
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<PCRResultRetrievalWorker>

    companion object {
        private val TAG = PCRResultRetrievalWorker::class.java.simpleName
    }
}
