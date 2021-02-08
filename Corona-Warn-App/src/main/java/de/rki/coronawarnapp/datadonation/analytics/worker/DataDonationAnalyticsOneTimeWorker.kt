package de.rki.coronawarnapp.datadonation.analytics.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber

/**
 * One time background Analytics worker
 *
 * @see DataDonationAnalyticsScheduler
 */
class DataDonationAnalyticsOneTimeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val analytics: Analytics
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
            analytics.submitIfWanted()
        } catch (e: Exception) {
            Timber.tag(TAG).d(e)
            result = Result.retry()
        }

        return result
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<DataDonationAnalyticsOneTimeWorker>

    companion object {
        private val TAG = DataDonationAnalyticsOneTimeWorker::class.java.simpleName
    }
}
