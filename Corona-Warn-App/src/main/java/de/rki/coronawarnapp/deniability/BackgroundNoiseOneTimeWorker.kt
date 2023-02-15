package de.rki.coronawarnapp.deniability

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber

/**
 * One time background noise worker
 */

@HiltWorker
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
            result = if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
                Result.failure()
            } else {
                Result.retry()
            }
        }

        Timber.tag(TAG).d("$id: doWork() finished with %s", result)
        return result
    }

    companion object {
        private val TAG = tag<BackgroundNoiseOneTimeWorker>()
    }
}
