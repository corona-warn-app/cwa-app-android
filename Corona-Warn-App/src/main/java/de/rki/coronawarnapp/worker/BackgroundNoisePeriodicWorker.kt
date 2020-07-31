package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.storage.LocalData

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
     * @see LocalData.isTestResultNotificationSent
     * @see LocalData.initialPollingForTestResultTimeStamp
     */
    override suspend fun doWork(): Result {
        TODO("Worker implementation")
    }
}
