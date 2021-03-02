package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.stop
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber

/**
 * Periodic background noise worker
 *
 * @see BackgroundWorkScheduler
 */
class BackgroundNoisePeriodicWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val submissionSettings: SubmissionSettings
) : CoroutineWorker(context, workerParams) {

    /**
     * @see BackgroundConstants.NUMBER_OF_DAYS_TO_RUN_PLAYBOOK
     */
    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")

        var result = Result.success()
        try {
            val initialPairingDate = submissionSettings.devicePairingSuccessfulAt ?: Instant.ofEpochMilli(0)

            // Check if the numberOfDaysToRunPlaybook are over
            if (initialPairingDate.plus(Duration.standardDays(NUMBER_OF_DAYS_TO_RUN_PLAYBOOK)).isBeforeNow) {
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

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<BackgroundNoisePeriodicWorker>

    companion object {
        private val TAG = BackgroundNoisePeriodicWorker::class.java.simpleName
        private val NUMBER_OF_DAYS_TO_RUN_PLAYBOOK = BackgroundConstants.NUMBER_OF_DAYS_TO_RUN_PLAYBOOK.toLong()
    }
}
