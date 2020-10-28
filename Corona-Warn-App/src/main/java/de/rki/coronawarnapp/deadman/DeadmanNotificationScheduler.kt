package de.rki.coronawarnapp.deadman

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dagger.Reusable
import javax.inject.Inject

@Reusable
class DeadmanNotificationScheduler @Inject constructor(
    val timeCalculation: DeadmanNotificationTimeCalculation,
    val workManager: WorkManager,
    val workBuilder: DeadmanNotificationWorkBuilder
) {

    /**
     * Enqueue background deadman notification onetime work
     * Replace with new if older work exists.
     */
    suspend fun scheduleOneTime() {
        // Get initial delay
        val delay = timeCalculation.getDelay()

        if(delay < 0) {
            return
        } else {
            //Create unique work and enqueue
            workManager.enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workBuilder.buildOneTimeWork(delay)
            )
        }
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
            workBuilder.buildPeriodicWork()
        )
    }

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
