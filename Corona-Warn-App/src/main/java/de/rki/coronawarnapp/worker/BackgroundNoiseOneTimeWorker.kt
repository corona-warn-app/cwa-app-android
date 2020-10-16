package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.util.di.AppInjector

/**
 * One time background noise worker
 *
 * @see BackgroundWorkScheduler
 */
class BackgroundNoiseOneTimeWorker(
    val context: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(context, workerParams) {

    private val playbook: Playbook
        get() = AppInjector.component.playbook

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
}
