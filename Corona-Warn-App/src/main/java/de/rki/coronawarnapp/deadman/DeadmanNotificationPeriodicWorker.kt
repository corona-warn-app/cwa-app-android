package de.rki.coronawarnapp.deadman

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber

/**
 * Periodic background deadman notification worker
 *
 * @see DeadmanNotificationScheduler
 */
class DeadmanNotificationPeriodicWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduler: DeadmanNotificationScheduler
) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("Background job started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.tag(TAG).d("Background job failed after $runAttemptCount attempts. Rescheduling")

            return Result.failure()
        }
        var result = Result.success()
        try {
            // Schedule one time deadman notification send work
            scheduler.scheduleOneTime()
        } catch (e: Exception) {
            Timber.tag(TAG).d(e)
            result = Result.retry()
        }

        return result
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<DeadmanNotificationPeriodicWorker>

    companion object {
        private val TAG = DeadmanNotificationPeriodicWorker::class.java.simpleName
    }
}
