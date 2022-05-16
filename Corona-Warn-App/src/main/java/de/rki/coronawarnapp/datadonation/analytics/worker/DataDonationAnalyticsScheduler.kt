package de.rki.coronawarnapp.datadonation.analytics.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import dagger.Reusable
import de.rki.coronawarnapp.initializer.Initializer
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
) : Initializer {

    /**
     * Enqueue background analytics submission periodic work
     */
    override fun initialize() {
        val initialDelay = timeCalculation.getDelay()
        Timber.d("schedulePeriodic() initialDelay(if not yet scheduled)=%s", initialDelay)
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
