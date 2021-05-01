package de.rki.coronawarnapp.deniability

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

@Reusable
class NoiseScheduler @Inject constructor(
    private val workManager: WorkManager,
) {

    fun setPeriodicNoise(enabled: Boolean) {
        Timber.tag(TAG).d("setPeriodicNoise(enabled=$enabled)")
        if (enabled) {
            enqueueBackgroundNoisePeriodicWork()
        } else {
            workManager.cancelUniqueWork(BACKGROUND_NOISE_PERIODIC_WORK_NAME)
        }
    }

    fun scheduleBackgroundNoiseOneTimeWork() {
        workManager.enqueueUniqueWork(
            BACKGROUND_NOISE_ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            buildBackgroundNoiseOneTimeWork()
        )
    }

    private fun enqueueBackgroundNoisePeriodicWork() = workManager.enqueueUniquePeriodicWork(
        BACKGROUND_NOISE_PERIODIC_WORK_NAME,
        ExistingPeriodicWorkPolicy.REPLACE,
        buildBackgroundNoisePeriodicWork()
    )

    private fun buildBackgroundNoiseOneTimeWork() =
        OneTimeWorkRequestBuilder<BackgroundNoiseOneTimeWorker>()
            .addTag(BACKGROUND_NOISE_ONE_TIME_WORKER_TAG)
            .setConstraints(
                Constraints.Builder().apply {
                    setRequiredNetworkType(NetworkType.CONNECTED)
                }.build()
            )
            .setInitialDelay(
                getBackgroundNoiseOneTimeWorkDelay(),
                TimeUnit.HOURS
            ).setBackoffCriteria(
                BackoffPolicy.LINEAR,
                BackgroundConstants.KIND_DELAY,
                TimeUnit.MINUTES
            )
            .build()

    /**
     * Build background noise periodic work request
     * Set "kind delay" for accessibility reason.
     *
     * @return PeriodicWorkRequest
     *
     * @see BackgroundConstants.MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION
     */
    private fun buildBackgroundNoisePeriodicWork() =
        PeriodicWorkRequestBuilder<BackgroundNoisePeriodicWorker>(
            BackgroundConstants.MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION,
            TimeUnit.HOURS
        )
            .addTag(BACKGROUND_NOISE_PERIODIC_WORKER_TAG)
            .setInitialDelay(
                BackgroundConstants.KIND_DELAY,
                TimeUnit.SECONDS
            ).setBackoffCriteria(
                BackoffPolicy.LINEAR,
                BackgroundConstants.KIND_DELAY,
                TimeUnit.MINUTES
            )
            .build()

    companion object {
        /**
         * Tag for background noise playbook periodic work
         */
        const val BACKGROUND_NOISE_PERIODIC_WORKER_TAG = "BACKGROUND_NOISE_PERIODIC_WORKER"

        /**
         * Tag for background noise playbook one time work
         */
        const val BACKGROUND_NOISE_ONE_TIME_WORKER_TAG = "BACKGROUND_NOISE_PERIODIC_WORKER"

        /**
         * Unique name for background noise playbook periodic work
         */
        const val BACKGROUND_NOISE_PERIODIC_WORK_NAME = "BackgroundNoisePeriodicWork"

        /**
         * Unique name for background noise playbook one time work
         */
        const val BACKGROUND_NOISE_ONE_TIME_WORK_NAME = "BackgroundNoiseOneTimeWork"

        private const val TAG = "NoiseScheduler"

        /**
         * Get background noise one time work delay
         * The periodic job is already delayed by MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION
         * so we only need to delay further by the difference between min and max.
         *
         * @return Long
         *
         * @see BackgroundConstants.MAX_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION
         * @see BackgroundConstants.MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION
         */
        fun getBackgroundNoiseOneTimeWorkDelay() = Random.nextLong(
            0,
            BackgroundConstants.MAX_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION -
                BackgroundConstants.MIN_HOURS_TO_NEXT_BACKGROUND_NOISE_EXECUTION
        )
    }
}
