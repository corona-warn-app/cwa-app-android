package de.rki.coronawarnapp.datadonation.analytics.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber

/**
 * Periodic background Analytics worker
 *
 * @see DataDonationAnalyticsScheduler
 */

@HiltWorker
class DataDonationAnalyticsPeriodicWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val analytics: Analytics
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("Background job started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.tag(TAG).d("Background job failed after $runAttemptCount attempts. Rescheduling")

            return Result.failure()
        }

        return try {
            val analyticsResult = analytics.submitIfWanted()
            Timber.tag(TAG).d("submitIfWanted() finished: %s", analyticsResult)
            when {
                analyticsResult.successful -> Result.success()
                analyticsResult.shouldRetry -> Result.retry()
                else -> Result.failure()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "submitIfWanted() failed unexpectedly")
            Result.failure()
        }
    }

    companion object {
        private val TAG = tag<DataDonationAnalyticsPeriodicWorker>()
    }
}
