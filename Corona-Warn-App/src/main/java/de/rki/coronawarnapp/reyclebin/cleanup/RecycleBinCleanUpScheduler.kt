package de.rki.coronawarnapp.reyclebin.cleanup

import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class RecycleBinCleanUpScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    fun setup() {
        Timber.d("setup()")
        scheduleOneTimeWorker()
    }

    private fun scheduleOneTimeWorker() {
        Timber.v("Setting up one time worker for recycle bin clean up")
        workManager.enqueueUniqueWork(UNIQUE_WORKER_NAME, ExistingWorkPolicy.KEEP, buildWorkRequest())
    }

    private fun buildWorkRequest() = OneTimeWorkRequestBuilder<RecycleBinCleanUpWorker>()
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            BackgroundConstants.KIND_DELAY,
            TimeUnit.MINUTES
        )
        .build()
}

private const val UNIQUE_WORKER_NAME = "BoosterCheckWorker"
