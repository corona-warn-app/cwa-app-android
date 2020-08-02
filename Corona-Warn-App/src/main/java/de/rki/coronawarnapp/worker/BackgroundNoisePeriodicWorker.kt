package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.http.playbook.BackgroundNoise
import de.rki.coronawarnapp.service.submission.SubmissionConstants
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.stop
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * Periodic background noise worker
 *
 * @see BackgroundWorkScheduler
 */
class BackgroundNoisePeriodicWorker(
    val context: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG: String? = BackgroundNoisePeriodicWorker::class.simpleName
    }

    /**
     * Work execution
     *
     * @return Result
     *
     * @see SubmissionConstants.numberOfDaysToRunPlaybook
     */
    override suspend fun doWork(): Result {
        val initialPairingDate = DateTime(
            LocalData.devicePairingSuccessfulTimestamp(),
            DateTimeZone.UTC
        )

        // Check if the numberOfDaysToRunPlaybook are over
        if (initialPairingDate.plusDays(SubmissionConstants.numberOfDaysToRunPlaybook).isBeforeNow) {
            stopWorker()
            return Result.success()
        }

        try {
            BackgroundNoise.getInstance().runDummyPlaybook()
        } catch (e: Exception) {
            // TODO: Should we even retry here?
            return if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                Result.failure()
            } else {
                Result.retry()
            }
        }

        return Result.success()
    }

    private fun stopWorker() {
        BackgroundWorkScheduler.WorkType.BACKGROUND_NOISE_PERIODIC_WORKER.stop()
    }
}
