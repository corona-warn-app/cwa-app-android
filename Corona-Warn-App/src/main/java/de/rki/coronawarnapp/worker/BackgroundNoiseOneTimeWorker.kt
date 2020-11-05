package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory

/**
 * One time background noise worker
 *
 * @see BackgroundWorkScheduler
 */
class BackgroundNoiseOneTimeWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val playbook: Playbook
) : CoroutineWorker(context, workerParams) {

    /**
     * Work execution
     *
     * @return Result
     */
    override suspend fun doWork(): Result {
        var result = Result.success()

        try {
            playbook.dummy()
        } catch (e: Exception) {
            // TODO: Should we even retry here?
            result = if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                Result.failure()
            } else {
                Result.retry()
            }
        }

        return result
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<BackgroundNoiseOneTimeWorker>
}
