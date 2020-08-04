package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.http.playbook.BackgroundNoise
import de.rki.coronawarnapp.http.playbook.PlaybookImpl
import kotlinx.coroutines.coroutineScope

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

    companion object {
        private val TAG: String? = BackgroundNoiseOneTimeWorker::class.simpleName
    }

    /**
     * Work execution
     *
     * @return Result
     */
    override suspend fun doWork(): Result = coroutineScope {
        var result = Result.success()

        try {
            PlaybookImpl(WebRequestBuilder.getInstance(), this)
                .dummy()
        } catch (e: Exception) {
            // TODO: Should we even retry here?
            result = if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                Result.failure()
            } else {
                Result.retry()
            }
        }

        return@coroutineScope result
    }

}
