package de.rki.coronawarnapp.datadonation.analytics.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import dagger.Reusable
import timber.log.Timber
import javax.inject.Inject

/**
 * We schedule two workers to allow for randomness in the execution times,
 * first a periodic worker is scheduled to run every 24 Hours.
 * We then schedule a one time workload featuring a random delay between 0 and 23 Hours
 * every time the periodic work is run, allowing us to distribute execution of analytics randomly
 */
@Reusable
class DataDonationAnalyticsScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val workBuilder: DataDonationAnalyticsWorkBuilder,
    private val timeCalculation: DataDonationAnalyticsTimeCalculation
) {

    /**
     * Enqueue background analytics submission periodic work
     */
    fun schedulePeriodic() {
        val initialDelay = timeCalculation.getDelay()

        Timber.d(
            "scheduling Analytics job for %s Hours in the future, will repeat every 24 Hours from there",
            initialDelay.standardHours
        )

        // Create unique work and enqueue
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workBuilder.buildPeriodicWork(initialDelay)
        )
    }

    companion object {
        /**
         * Analytics Data Donation periodic work
         */
        const val PERIODIC_WORK_NAME = "DataDonationAnalyticsPeriodicWork"
    }
}
