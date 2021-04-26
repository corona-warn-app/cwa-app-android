package de.rki.coronawarnapp.coronatest.type.pcr.execution

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
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
            // PCRResultScheduler will cancel us
            if (coronaTestRepository.latestPCRT.first() == null) {
                Timber.tag(TAG).d(" $id Stopping worker, there is no PCR test.")
                return Result.success()
            }

            Timber.tag(TAG).d(" $id Running task.")
            val coronaTest = coronaTestRepository.refresh(
                type = CoronaTest.Type.PCR
            ).single() as PCRCoronaTest
            val testResult = coronaTest.testResult
            Timber.tag(TAG).d("$id: Test result retrieved is $testResult")

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
