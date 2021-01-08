package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.stop
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import timber.log.Timber

/**
 * Periodic background noise worker
 *
 * @see BackgroundWorkScheduler
 */
class BackgroundNoisePeriodicWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    /**
     * @see BackgroundConstants.NUMBER_OF_DAYS_TO_RUN_PLAYBOOK
     */
    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        var result = Result.success()
        try {
            val initialPairingDate = DateTime(
                LocalData.devicePairingSuccessfulTimestamp(),
                DateTimeZone.UTC
            )

            // Check if the numberOfDaysToRunPlaybook are over
            if (initialPairingDate.plusDays(BackgroundConstants.NUMBER_OF_DAYS_TO_RUN_PLAYBOOK).isBeforeNow) {
                stopWorker()
                return result
            }

            BackgroundWorkScheduler.scheduleBackgroundNoiseOneTimeWork()
        } catch (e: Exception) {
            result = if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
        Timber.tag(TAG).d("$id: doWork() finished with %s", result)
        return result
    }

    private fun stopWorker() {
        BackgroundWorkScheduler.WorkType.BACKGROUND_NOISE_PERIODIC_WORK.stop()
        Timber.tag(TAG).d("$id: worker stopped")
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<BackgroundNoisePeriodicWorker>

    companion object {
        private val TAG = BackgroundNoisePeriodicWorker::class.java.simpleName
    }
}
