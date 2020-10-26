package de.rki.coronawarnapp.deadman

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import dagger.Reusable
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.worker.BackgroundConstants
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class DeadmanNotificationScheduler @Inject constructor() {

    /**
     * Work manager instance
     */
    private val workManager by lazy { WorkManager.getInstance(CoronaWarnApplication.getAppContext()) }

    /**
     * Enqueue background deadman notification onetime work
     * Replace with new if older work exists.
     */
    fun scheduleOneTime(lastSuccess: DateTime?) {
        Timber.d("Scheduling one time job...")
        // Calculate initial delay
//        val delay = DeadmanNotificationTimeCalculation().delay(lastSuccess)
        val delay = 0L
        //Create unique work and enqueue
        workManager.enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            buildOneTimeWork(delay)
        )
    }

    /**
     * Enqueue background deadman notification onetime work
     * Replace with new if older work exists.
     */
    fun schedulePeriodic() {
        //Create unique work and enqueue
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            buildPeriodicWork()
        )
    }

    /**
     * Build one time work
     */
    private fun buildOneTimeWork(delay: Long) =
        OneTimeWorkRequestBuilder<DeadmanNotificationOneTimeWorker>()
            .setInitialDelay(
                delay,
                TimeUnit.MINUTES
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BackgroundConstants.BACKOFF_INITIAL_DELAY,
                TimeUnit.MINUTES
            )
            .build()
//            .also { it.workSpec. } TODO: add here

    /**
     * Build periodic work
     */
    private fun buildPeriodicWork() = PeriodicWorkRequestBuilder<DeadmanNotificationPeriodicWorker>(
        DateTimeConstants.HOURS_PER_DAY.toLong(), TimeUnit.MINUTES
    )
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            BackgroundConstants.BACKOFF_INITIAL_DELAY,
            TimeUnit.MINUTES
        )
        .build()

    companion object {
        /**
         * Deadman notification one time work
         */
        const val ONE_TIME_WORK_NAME = "DeadmanNotificationOneTimeWork";
        /**
         * Deadman notification periodic work
         */
        const val PERIODIC_WORK_NAME = "DeadmanNotificationPeriodicWork";
    }
}
