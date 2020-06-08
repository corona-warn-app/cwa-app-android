package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.BuildConfig
import timber.log.Timber

/**
 * Periodic diagnosis key retrieval work
 * Executes the scheduling of one time diagnosis key retrieval work
 *
 * @see BackgroundWorkScheduler
 * @see DiagnosisKeyRetrievalOneTimeWorker
 */
class DiagnosisKeyRetrievalPeriodicWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG: String? = DiagnosisKeyRetrievalPeriodicWorker::class.simpleName
    }

    /**
     * Work execution
     *
     * @return Result
     *
     * @see BackgroundWorkScheduler.scheduleDiagnosisKeyPeriodicWork()
     * @see BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()
     */
    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) Timber.d("Background job started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            if (BuildConfig.DEBUG) Timber.d("Background job failed after $runAttemptCount attempts. Rescheduling")
            BackgroundWorkScheduler.scheduleDiagnosisKeyPeriodicWork()
            return Result.failure()
        }
        var result = Result.success()
        try {
            BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()
        } catch (e: Exception) {
            result = Result.retry()
        }
        return result
    }
}
