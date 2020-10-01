package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        BackgroundWorkHelper.sendDebugNotification(
            "KeyPeriodic Executing: Start", "KeyPeriodic started. Run attempt: $runAttemptCount"
        )

        var result = Result.success()
        try {
            BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()
        } catch (e: Exception) {
            Timber.tag(TAG).w(
                e, "$id: Error during BackgroundWorkScheduler.scheduleDiagnosisKeyOneTimeWork()."
            )

            if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                Timber.tag(TAG).w(e, "$id: Retry attempts exceeded.")

                BackgroundWorkHelper.sendDebugNotification(
                    "KeyPeriodic Executing: Failure",
                    "KeyPeriodic failed with $runAttemptCount attempts"
                )

                return Result.failure()
            } else {
                Timber.tag(TAG).d(e, "$id: Retrying.")
                result = Result.retry()
            }
        }

        BackgroundWorkHelper.sendDebugNotification(
            "KeyPeriodic Executing: End", "KeyPeriodic result: $result "
        )

        Timber.tag(TAG).d("$id: doWork() finished with %s", result)
        return result
    }
}
