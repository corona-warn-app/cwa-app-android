package de.rki.coronawarnapp.datadonation.analytics.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import dagger.Reusable
import javax.inject.Inject

@Reusable
class DataDonationAnalyticsScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val workBuilder: DataDonationAnalyticsWorkBuilder
) {

    /**
     * Enqueue background deadman notification onetime work
     * Do -NOT- Replace with new if older work exists.
     * As this will result in the notification spam seen in the past
     * because running a new check every time the app is opened
     * spawns a notification every time the last check
     * was more than 35 Hours ago.
     */
    fun schedulePeriodic() {
        // Create unique work and enqueue
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workBuilder.buildPeriodicWork()
        )
    }

    companion object {
        /**
         * Analytics Data Donation periodic work
         */
        const val PERIODIC_WORK_NAME = "DataDonationAnalyticsPeriodicWork"
    }
}
