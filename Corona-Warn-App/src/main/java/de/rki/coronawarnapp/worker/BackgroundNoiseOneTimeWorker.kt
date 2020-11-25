package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import timber.log.Timber

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

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("$id: doWork() started. Run attempt: $runAttemptCount")
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

        Timber.tag(TAG).d("$id: doWork() finished with %s", result)
        return result
    }

    @AssistedInject.Factory
    interface Factory : InjectedWorkerFactory<BackgroundNoiseOneTimeWorker>

    companion object {
        private val TAG = BackgroundNoiseOneTimeWorker::class.java.simpleName
    }
}
