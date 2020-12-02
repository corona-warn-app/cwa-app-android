package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

/**
 * Periodic diagnosis key retrieval work
 * Executes the scheduling of one time diagnosis key retrieval work
 *
 * @see BackgroundWorkScheduler
 * @see DiagnosisKeyRetrievalOneTimeWorker
 */
class DiagnosisKeyRetrievalPeriodicWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    /**
     * @see BackgroundWorkScheduler.scheduleDiagnosisKeyPeriodicWork()
     * @see BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()
     */
    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        var result = Result.success()
        try {
            BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()
        } catch (e: Exception) {
            Timber.tag(TAG).w(
                e, "$id: Error during BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()."
            )

            if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                Timber.tag(TAG).w(e, "$id: Retry attempts exceeded.")

                return Result.failure()
            } else {
                Timber.tag(TAG).d(e, "$id: Retrying.")
                result = Result.retry()
            }
        }

        Timber.tag(TAG).d("$id: doWork() finished with %s", result)
        return result
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<DiagnosisKeyRetrievalPeriodicWorker>

    companion object {
        private val TAG = DiagnosisKeyRetrievalPeriodicWorker::class.java.simpleName
    }
}
